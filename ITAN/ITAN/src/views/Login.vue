<template>
  <div class="BG">
    <div class="box" ref="box">
      <!-- 左侧欢迎界面 -->
      <div class="left-box">
        <h1 data-aos="fade-up" class="welcome">欢迎使用易探大模型</h1>
        <p data-aos="fade-up">Welcome!</p>
        <div data-aos="fade-up" class="img-box">
          <img src="@/assets/img/yj.png" alt="" id="avatar" />
        </div>
      </div>

      <!-- 登录表单 -->
      <div class="login-form">
        <v-container fluid>
          <v-row>
            <v-col lg="6"></v-col>
            <v-col lg="6">
              <div data-aos="fade-up" class="title-box">
                <h1 style="font-family: KaiTi,serif;font-size:45px">登录</h1>
              </div>
              <v-row data-aos="fade-up" align="center" justify="center">
                <v-col cols="12" lg="8" style="max-width: 400px;">
                  <v-form ref="formRef" v-model="valid">
                    <v-text-field
                        v-model="form.email"
                        :rules="emailRules"
                        placeholder="请输入邮箱"
                        required
                        outlined
                        color="primary"
                        class="input-field"
                        :append-inner-icon="'mdi-email'"
                        style="padding-right: 40px"
                    />

                    <v-text-field
                        v-model="form.password"
                        :rules="passwordRules"
                        :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                        @click:append="showPassword = !showPassword"
                        :type="showPassword ? 'text' : 'password'"
                        placeholder="请输入密码"
                        required
                        outlined
                        color="primary"
                        class="input-field"
                    />


                  </v-form>
                  <v-row>
                    <v-col lg="6">
                      <v-btn dark color="#340e0e" @click="login">登录</v-btn>
                    </v-col>
                    <v-col lg="6" class="text-right">
                      <v-btn text dark @click="toRegister">没有账号？去注册</v-btn>
                    </v-col>
                  </v-row>
                </v-col>
              </v-row>
            </v-col>
          </v-row>
        </v-container>
      </div>
    </div>
  </div>
</template>

<script setup>
import {ref} from 'vue'
import {useRouter} from 'vue-router'
import request from '@/utils/request'

const router = useRouter()
const valid = ref(false)
const formRef = ref(null)

const form = ref({
  email: '',
  password: ''
})

const showPassword = ref(false)

const emailRules = [
  v => !!v || '请输入邮箱',
  v => (v.includes('@') ? true : '邮箱必须包含@符号')
]

const passwordRules = [
  v => !!v || '请输入密码',
  v => (v && v.trim().length !== 0) || '请输入密码',
  v => (v && v.length <= 18) || '密码长度不超过18位',
  v => (v && v.length >= 8) || '密码长度不少于8位'
]

const login = async () => {
  formRef.value?.validate()
  if (valid.value) {
    try {
      const response = await request.post('/user/login', form.value)
      if (String(response.code) === '200') {
        localStorage.setItem('token', response.data.token)
        localStorage.setItem('userId', response.data.id)
        localStorage.setItem('nickname', response.data.nickname)
        router.push({path: '/HomeView'})
        console.log(localStorage)
        console.log(localStorage.getItem('userId'))
      } else {
        alert(response.msg || '登录失败')
      }
    } catch (error) {
      console.error('登录请求失败', error)
      alert('服务器错误，请稍后重试')
    }
  }
}

const toRegister = () => {
  router.push('/register')
}
</script>

<style scoped>
.BG {
  height: 100vh;
  overflow-x: hidden;
  display: flex;
  background-image: url(@/assets/img/page-header.png);
  margin: 0;
  background-size: 100% 100%;
  background-attachment: fixed;
}

.box {
  width: 1050px;
  height: 600px;
  display: flex;
  position: relative;
  z-index: 2;
  margin: auto;
  border-radius: 50px;
  border: 1px solid rgba(255, 255, 255, 0.6);
  box-shadow: 2px 1px 19px rgba(0, 0, 0, 0.1);
}

.welcome {
  font-family: KaiTi, serif;
  font-size: 45px;
}

.left-box {
  width: 50%;
  height: 100%;
  position: absolute;
  left: 0;
  top: 0;
  z-index: 99;
  border-radius: 50px 0px 0px 50px;
  background-color: #340e0e;
  box-shadow: 2px 1px 19px rgba(0, 0, 0, 0.1);
}

.left-box h1 {
  margin-top: 150px;
  text-align: center;
  letter-spacing: 5px;
  color: #eeeeee;
  user-select: none;
  text-shadow: 4px 4px 3px rgba(0, 0, 0, 0.1);
}

.left-box p {
  height: 30px;
  line-height: 30px;
  text-align: center;
  margin: 20px 0;
  user-select: none;
  font-weight: bold;
  color: #eeeeee;
  text-shadow: 4px 4px 3px rgba(0, 0, 0, 0.1);
}

.img-box {
  width: 300px;
  height: 250px;
  margin: 20px auto;
  user-select: none;
  overflow: hidden;
}

.img-box img {
  width: 100%;
}

.login-form {
  flex: 1;
  height: 100%;
  background-color: #4e1717;
  border-radius: 50px;

  display: flex;
  flex-direction: column;
  justify-content: center; /* 垂直居中 */
  align-items: center; /* 水平居中 */
}

.title-box {
  margin-bottom: 20px;
  text-align: center;
}

.title-box h1 {
  text-align: center;
  color: #e2e2e2;
  user-select: none;
  letter-spacing: 5px;
  text-shadow: 4px 4px 3px rgba(0, 0, 0, 0.1);
}

::v-deep .v-input__control {
  background-color: white !important;
  border-radius: 6px;
  width: 325px;
}

.input-field {
  width: 100%;
  max-width: 400px;
  margin-bottom: 20px;
}
</style>
