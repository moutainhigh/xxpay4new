webpackJsonp([10],{"8KWv":function(t,e){},Kw6W:function(t,e,i){"use strict";Object.defineProperty(e,"__esModule",{value:!0});var a=i("CJJO"),n=i("WBIT"),s=i("s0MJ").dateUtils,o={data:function(){return{htmlProportion:"",htmlWidth:"",htmlheight:"",date:new Date,dateShow:!1,open:!1,openScroll:!1,tel:"",memberName:"",avatar:"",sex:"",sexDetail:"",birthday:""}},created:function(){this.htmlWidth=document.documentElement.clientWidth||document.body.clientWidth,this.htmlheight=document.documentElement.clientHeight||document.body.clientHeight,this.htmlProportion=this.htmlWidth/750,this.getMember()},methods:{getMember:function(){var t=this;Object(a.k)().then(function(e){t.tel=e.data.tel,t.memberName=e.data.memberName,t.avatar=e.data.avatar,t.sexDetail=1==e.data.sex?"男":2==e.data.sex?"女":"未知",t.birthday=e.data.birthday?s.format2Date(e.data.birthday):""}).catch(function(t){console.log(t)})},update:function(){var t=this;Object(a.z)(this.avatar,this.memberName,this.sex,this.birthday).then(function(e){n.Toast.text({duration:1500,message:"修改成功"}),setTimeout(function(){t.$router.push({path:"/user"})},1500)}).catch(function(t){console.log(t)})},dateSelect:function(){this.dateShow=!0},confirm:function(){this.birthday=this.format2Date(this.date),this.dateShow=!1},format2Date:function(t){var e=function(t){return t<10?"0"+t:t};return t.getFullYear()+"-"+e(t.getMonth()+1)+"-"+e(t.getDate())},cancle:function(){this.dateShow=!1},openDialog:function(){this.openScroll=!0},closeDialog:function(){this.openScroll=!1},closeBottomSheet:function(){this.open=!1},openBotttomSheet:function(){this.open=!0},sexNan:function(){this.sex=1,this.sexDetail="男"},sexNv:function(){this.sex=2,this.sexDetail="女"},upLoadImg:function(){this.$router.push({path:"/user/upLoadImg"})}}},l={render:function(){var t=this,e=t.$createElement,i=t._self._c||e;return i("div",{staticClass:"center",style:{height:t.htmlheight+"px"}},[i("div",{staticClass:"uni-list"},[i("div",{staticClass:"uni-list-cell",on:{click:function(e){return t.upLoadImg()}}},[i("div",{staticClass:"uni-list-cell-navigate uni-navigate-right uni-media-list"},[i("div",{staticClass:"uni-media-list-body"},[i("span",{staticClass:"text-gray"},[t._v("头像")]),t._v(" "),i("mu-avatar",{attrs:{size:"55"}},[i("img",{attrs:{src:t.avatar}})])],1)])])]),t._v(" "),i("div",{staticClass:"uni-list  margin-top"},[i("div",{staticClass:"uni-list-cell"},[i("div",{staticClass:"uni-media-list"},[i("div",{staticClass:"uni-media-list-body"},[i("span",{staticClass:"text-gray"},[t._v("手机号")]),t._v(" "),i("span",[t._v(t._s(t.tel))])])])]),t._v(" "),i("div",{staticClass:"uni-list-cell",on:{click:function(e){return t.openDialog()}}},[i("div",{staticClass:"uni-list-cell-navigate uni-navigate-right uni-media-list"},[i("div",{staticClass:"uni-media-list-body"},[i("span",{staticClass:"text-gray"},[t._v("会员名称")]),t._v(" "),i("span",[t._v(t._s(t.memberName))])])])]),t._v(" "),i("div",{staticClass:"uni-list-cell",on:{click:function(e){return t.openBotttomSheet()}}},[i("div",{staticClass:"uni-list-cell-navigate uni-navigate-right uni-media-list"},[i("div",{staticClass:"uni-media-list-body"},[i("span",{staticClass:"text-gray"},[t._v("性别")]),t._v(" "),i("span",[t._v(t._s(t.sexDetail))])])])]),t._v(" "),i("div",{staticClass:"uni-list-cell",on:{click:function(e){return t.dateSelect()}}},[i("div",{staticClass:"uni-list-cell-navigate uni-navigate-right uni-media-list"},[i("div",{staticClass:"uni-media-list-body"},[i("span",{staticClass:"text-gray"},[t._v("生日")]),t._v(" "),i("span",[t._v(t._s(t.birthday))])])])])]),t._v(" "),i("mu-dialog",{attrs:{title:"会员名称",width:"360",open:t.openScroll},on:{"update:open":function(e){t.openScroll=e}}},[i("mu-text-field",{attrs:{placeholder:"请输入会员名称"},model:{value:t.memberName,callback:function(e){t.memberName=e},expression:"memberName"}}),i("br"),t._v(" "),i("mu-button",{attrs:{slot:"actions",flat:"",color:"primary"},on:{click:t.closeDialog},slot:"actions"},[t._v("确定")])],1),t._v(" "),t.dateShow?i("mu-container",{staticClass:"date-position"},[i("mu-flex",{attrs:{"justify-content":"center","align-items":"center",wrap:"wrap"}},[i("mu-paper",{staticClass:"demo-date-picker",attrs:{"z-depth":1}},[i("mu-date-picker",{attrs:{date:t.date},on:{"update:date":function(e){t.date=e}}}),t._v(" "),i("mu-flex",{staticStyle:{"padding-bottom":"10px"},attrs:{"justify-content":"around"}},[i("mu-button",{attrs:{small:"",color:"primary"},on:{click:function(e){return t.cancle()}}},[t._v("取消")]),t._v(" "),i("mu-button",{attrs:{small:"",color:"primary"},on:{click:function(e){return t.confirm()}}},[t._v("确定")])],1)],1)],1)],1):t._e(),t._v(" "),t.dateShow?i("div",{staticClass:"zx-overlay",on:{click:function(e){return t.cancle()}}}):t._e(),t._v(" "),i("mu-container",[i("mu-bottom-sheet",{attrs:{open:t.open},on:{"update:open":function(e){t.open=e}}},[i("mu-list",{on:{"item-click":t.closeBottomSheet}},[i("mu-list-item",{attrs:{button:""},on:{click:function(e){return t.sexNan()}}},[i("mu-list-item-title",{staticStyle:{"text-align":"center"}},[t._v("男")])],1),t._v(" "),i("mu-list-item",{attrs:{button:""},on:{click:function(e){return t.sexNv()}}},[i("mu-list-item-title",{staticStyle:{"text-align":"center"}},[t._v("女")])],1)],1)],1)],1),t._v(" "),i("mu-flex",{attrs:{"justify-content":"center"}},[i("mu-button",{staticStyle:{width:"80%","margin-top":"20px"},attrs:{round:"",color:"#FF9000"},on:{click:function(e){return t.update()}}},[t._v("确定")])],1)],1)},staticRenderFns:[]};var c=i("C7Lr")(o,l,!1,function(t){i("8KWv")},null,null);e.default=c.exports}});