//引入上边封装的axios文件
import request from "@/request";
// import QS from "qs";


//获取用户信息  post请求
export const getUserInfo = data => {
    return request({
        url: "/user/info",
        method: "POST",
        headers:{
            "content-type": "application/json;charset=UTF-8"
        },
        data
    });
};

//下载用户上传模板   get请求
export const downLoadTemplate = params=> {
    return request({
        url: "/user/info/template",
        method: "GET",
        headers:{
            "content-type": "application/json;charset=UTF-8"
        },
        params
    });
};

