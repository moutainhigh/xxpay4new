package org.xxpay.isv.common.service;

import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.google.gson.JsonObject;
import me.chanjar.weixin.common.error.WxErrorException;
import me.chanjar.weixin.open.api.WxOpenComponentService;
import me.chanjar.weixin.open.api.WxOpenService;
import me.chanjar.weixin.open.api.impl.WxOpenComponentServiceImpl;
import me.chanjar.weixin.open.api.impl.WxOpenInMemoryConfigStorage;
import me.chanjar.weixin.open.api.impl.WxOpenServiceImpl;
import me.chanjar.weixin.open.bean.WxOpenComponentAccessToken;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.xxpay.core.common.Exception.ServiceException;
import org.xxpay.core.common.constant.MchConstant;
import org.xxpay.core.common.constant.RetEnum;
import org.xxpay.core.common.vo.WxAuthVO;
import org.xxpay.core.entity.IsvWx3rdInfo;
import org.xxpay.core.entity.MchWxauthInfo;

import java.util.concurrent.TimeUnit;

@Service
public class XxPayWxComponentService {

    public static final String API_COMPONENT_OPEN_GET = "https://api.weixin.qq.com/cgi-bin/open/get";
    public static final String API_COMPONENT_OPEN_CREATE = "https://api.weixin.qq.com/cgi-bin/open/create";
    public static final String API_COMPONENT_OPEN_BIND = "https://api.weixin.qq.com/cgi-bin/open/bind";
    public static final String API_COMPONENT_OPEN_UNBIND = "https://api.weixin.qq.com/cgi-bin/open/unbind";

    /**
     * 代小程序实现业务.
     * 小程序代码模版库管理：https://open.weixin.qq.com/cgi-bin/showdocument?action=dir_list&t=resource/res_list&verify=1&id=open1506504150_nMMh6&token=&lang=zh_CN
     */
    public static final String GET_TEMPLATE_DRAFT_LIST_URL = "https://api.weixin.qq.com/wxa/gettemplatedraftlist";
    public static final String GET_TEMPLATE_LIST_URL = "https://api.weixin.qq.com/wxa/gettemplatelist";
    public static final String ADD_TO_TEMPLATE_URL = "https://api.weixin.qq.com/wxa/addtotemplate";
    public static final String DELETE_TEMPLATE_URL = "https://api.weixin.qq.com/wxa/deletetemplate";


    @Autowired
    private RpcCommonService rpc;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**获取binarywang weixin-java-open工具包中提供的service */
    public WxOpenComponentService getBinarywangWxOpenComponentService(Long currentIsvId) throws Exception{

        WxAuthVO wxAuthVO = getIsvAccessToken(currentIsvId);

        WxOpenInMemoryConfigStorage config = new WxOpenInMemoryConfigStorage();
        config.setComponentAppId(wxAuthVO.getAppId());
        config.setComponentToken(wxAuthVO.getAccessToken());
        config.updateComponentAccessToken(wxAuthVO.getAccessToken(), 7200);  //不失效， 避免util自动查询
        WxOpenService wxOpenService = new WxOpenServiceImpl();
        wxOpenService.setWxOpenConfigStorage(config);
        return new WxOpenComponentServiceImpl(wxOpenService);
    }

    /**获取 isv Token参数  */
    public WxAuthVO getIsvAccessToken(Long currentIsvId) throws Exception{

        //redis中获取:
        //当前服务商的appId
        String componentAppId = stringRedisTemplate.opsForValue().get(MchConstant.CACHEKEY_WX_COMPONENT_APP_ID + currentIsvId);

        //当前服务商的accessToken
        String componentAccessToken = stringRedisTemplate.opsForValue().get(MchConstant.CACHEKEY_WX_COMPONENT_ACCESS_TOKEN + currentIsvId);

        if(StringUtils.isAnyEmpty(componentAppId, componentAccessToken)){  //redis中不存在， 说明token已过期， 需要重新获取

            //数据库中获取该服务商的配置信息
            IsvWx3rdInfo isvWx3rdInfo = rpc.rpcIsvWx3rdInfoService.getById(currentIsvId);

            //查询 & 重新设置缓存中的 accessToken
            WxOpenComponentAccessToken wxResult = queryAndSetCacheAccessToken(currentIsvId, isvWx3rdInfo.getComponentAppId(),
                    isvWx3rdInfo.getComponentAppSecret(), isvWx3rdInfo.getComponentVerifyTicket());

            componentAppId = isvWx3rdInfo.getComponentAppId();
            componentAccessToken = wxResult.getComponentAccessToken();
        }

        return new WxAuthVO(componentAppId, componentAccessToken);
    }


    /** 接口查询 & 重新放置缓存 accessToken  **/
    public WxOpenComponentAccessToken queryAndSetCacheAccessToken(Long isvId,
                                                                  String componentAppId, String appSecret, String ticket) throws WxErrorException {
        //接口重新获取token
        //wixin-java-open 包中的 WxOpenComponentServiceImpl.getComponentAccessToken  无法获取到 expires_in 时间（私有变量 = = .）
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("component_appid", componentAppId);
        jsonObject.addProperty("component_appsecret", appSecret);
        jsonObject.addProperty("component_verify_ticket", ticket);

        WxOpenService reqService = new WxOpenServiceImpl();
        reqService.setWxOpenConfigStorage(new WxOpenInMemoryConfigStorage());  // init http
        String responseContent = reqService.post(WxOpenComponentService.API_COMPONENT_TOKEN_URL, jsonObject.toString());
        WxOpenComponentAccessToken wxResult = WxOpenComponentAccessToken.fromJson(responseContent);

        String componentAccessToken = wxResult.getComponentAccessToken();
        Integer expiresIn = wxResult.getExpiresIn();  //过期时间, 单位：s； 一般为7200s (2小时)

        //重新放置redis
        Integer redisExpiresIn = expiresIn - 60;  //redis过期时间为 默认时间减去1分钟, 避免接口传输时间导致已失效问题
        stringRedisTemplate.opsForValue().set(MchConstant.CACHEKEY_WX_COMPONENT_APP_ID + isvId, componentAppId, redisExpiresIn, TimeUnit.SECONDS);
        stringRedisTemplate.opsForValue().set(MchConstant.CACHEKEY_WX_COMPONENT_ACCESS_TOKEN + isvId, componentAccessToken, redisExpiresIn, TimeUnit.SECONDS);
        return wxResult;
    }

    /** 获取 第三方对商户公众号授权信息  **/
    public WxAuthVO getMchAccessToken(Long mchId, Byte authFrom) throws Exception {

        if (authFrom != MchConstant.MEMBER_OPENID_FROM_MINI_FOOD && authFrom != MchConstant.MEMBER_OPENID_FROM_MINI_MALL
                && authFrom != MchConstant.MEMBER_OPENID_FROM_MINI_PAY){
            throw ServiceException.build(RetEnum.RET_ISV_NOT_SUPPORT_WX_AUTHFROM);
        }

        //获取商户第三方授权信息
        MchWxauthInfo mchWxauthInfo = rpc.rpcMchWxauthInfoService.getOneByMchIdAndAuthFrom(mchId, authFrom);
        if (mchWxauthInfo == null || mchWxauthInfo.getAuthStatus() != 1){
            throw ServiceException.build(RetEnum.RET_ISV_WX_MCH_NOT_AUTH);
        }

        String authAppId = mchWxauthInfo.getAuthAppId();
        String authAccessToken = stringRedisTemplate.opsForValue().get(MchConstant.CACHEKEY_WX_AUTH_ACCESS_TOKEN + mchId + "_" + authFrom + "_" + authAppId);
        Integer expiresIn;

        if(StringUtils.isEmpty(authAccessToken)){ //为null

            //查询查询
            Long isvId = rpc.rpcMchInfoService.findByMchId(mchId).getIsvId();
            WxOpenComponentService service = this.getBinarywangWxOpenComponentService(isvId);

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("component_appid", service.getWxOpenConfigStorage().getComponentAppId());
            jsonObject.put("authorizer_appid", authAppId);
            jsonObject.put("authorizer_refresh_token", mchWxauthInfo.getAuthRefreshToken());

            JSONObject response = reqWXByIsv(isvId, WxOpenComponentService.API_AUTHORIZER_TOKEN_URL, jsonObject);

            authAccessToken = response.getString("authorizer_access_token");
            expiresIn = response.getInteger("expires_in");

            //重新放置redis
            Integer redisExpiresIn = expiresIn - 60;  //redis过期时间为 默认时间减去1分钟, 避免接口传输时间导致已失效问题
            stringRedisTemplate.opsForValue().set(MchConstant.CACHEKEY_WX_AUTH_ACCESS_TOKEN + mchId + "_" + authFrom + "_" + authAppId,
                    authAccessToken, redisExpiresIn, TimeUnit.SECONDS);
        }

        return new WxAuthVO(authAppId, authAccessToken);
    }


    /** 通用请求微信接口 **/
    public static JSONObject reqWX(String reqUrl, JSONObject params) throws WxErrorException{

        WxOpenService reqService = new WxOpenServiceImpl();
        reqService.setWxOpenConfigStorage(new WxOpenInMemoryConfigStorage());  // init http
        String responseContent = reqService.post(reqUrl, params.toString());
        return (JSONObject)JSONObject.parse(responseContent);
    }

    /** 商户接口 **/
    public JSONObject reqWXByMch(Long mchId, Byte authFrom, String reqUrl, JSONObject params) throws Exception {

        WxAuthVO wxAuthVO = getMchAccessToken(mchId, authFrom);
        if(StringUtils.isNotEmpty(wxAuthVO.getAccessToken())){
            reqUrl = reqUrl + "?access_token=" + wxAuthVO.getAccessToken();
        }
        return reqWX(reqUrl, params);
    }

    /** isv第三方接口 **/
    public JSONObject reqWXByIsv(Long isvId, String reqUrl, JSONObject params) throws Exception {

        String componentAccessToken = getIsvAccessToken(isvId).getAccessToken();
        if(StringUtils.isNotEmpty(componentAccessToken)){
            reqUrl = reqUrl + "?component_access_token=" + componentAccessToken;
        }
        return reqWX(reqUrl, params);

    }

    /** 商户接口   GET请求**/
    public JSONObject reqWXByMchGET(Long mchId, Byte authFrom, String reqUrl, JSONObject params) throws Exception {

        WxAuthVO wxAuthVO = getMchAccessToken(mchId, authFrom);
        if(StringUtils.isNotEmpty(wxAuthVO.getAccessToken())){
            reqUrl = reqUrl + "?access_token=" + wxAuthVO.getAccessToken();
        }
        return reqWXGET(reqUrl, params);
    }

    /** 通用请求微信接口   GET请求**/
    public static JSONObject reqWXGET(String reqUrl, JSONObject params) throws WxErrorException{

        WxOpenService reqService = new WxOpenServiceImpl();
        reqService.setWxOpenConfigStorage(new WxOpenInMemoryConfigStorage());  // init http
        String responseContent;
        if (params == null){
            responseContent = reqService.get(reqUrl, null);
        }else {
            responseContent = reqService.get(reqUrl, params.toString());
        }
        return (JSONObject)JSONObject.parse(responseContent);
    }



}
