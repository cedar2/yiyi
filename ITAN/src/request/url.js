const VUE_APP_MODE = process.env.VUE_APP_MODE;
let baseURL;
if (VUE_APP_MODE == 'development') { // 本地
    baseURL = 'http://127.0.0.1:8080';//测试地址
} else {
    // pre 预生产
    // prod 生产
    switch (VUE_APP_MODE) {
        case 'test':// 测试
            baseURL = 'http://test.com/api';
            break;
        case 'pre':// 预生产
            baseURL = 'http://pre.com';
            break;
        case 'prod':// 生产
            baseURL = 'http://prod.com';
            break;
        default:// 测试
            baseURL = 'http://test.com';
            break;
    }
}
module.exports = {
    baseURL
};
