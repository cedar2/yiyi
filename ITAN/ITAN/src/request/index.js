/**
 * 请求封装
 */
import axios from 'axios';
import configUrl from './url.js';
import store from '../store';

const service = axios.create({
    baseURL: configUrl.baseURL,
    // withCredentials: true, // 当跨域请求时发送cookie
    timeout: 15000 // 请求超时
});

//添加请求拦截器   请求接口统一添加token
service.interceptors.request.use(
    config =>{
        config.headers.token = store.getters.token || '';  //请求添加token
        return config;
    },
    error =>{
        return Promise.reject(error);
    }
)

// 响应拦截器
service.interceptors.response.use(
    response => {
        //如果接口返回token，替换本地旧token
        if (response.headers.token) {
            sessionStorage.setItem("token", response.headers.token);
        }
        //自定义设置后台返回code对应的响应方式
        if (response.data.code == 203) { // 未登录或登录超时
            return Promise.reject(new Error('登录超时'));
        } else { //接口正常，返回数据
            return response;
        }
    },
    error => {
        if (error.message) {
            // this.$massage.error('服务器开小差了，请稍后再试!')
            //错误响应
            alert('服务器开小差了，请稍后再试!')
        }
        return Promise.reject(error);
    }
);
//暴露出封装过的服务
export default service;
