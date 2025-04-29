<template>
  <div class="BG">
    <div class="box" ref="box">
      <div class="left-box">
        <h1 data-aos="fade-up" class="welcome">欢迎使用易探大模型</h1>
        <p data-aos="fade-up">Welcome!</p>
        <div data-aos="fade-up" class="img-box">
          <img src="../../public/assets/img/yj.png" alt="" id="avatar" />
        </div>
      </div>

      <div class="login-form">
        <v-container fluid>
          <v-row>
            <v-col lg="6"></v-col>
            <v-col lg="6">
              <div data-aos="fade-up" class="title-box">
                <h1>注册</h1>
              </div>
              <v-row data-aos="fade-up" align="center" justify="center">
                <v-col lg="8">
                  <v-form ref="formRef" v-model="valid" class="form">
                    <v-text-field v-model="form.email" :rules="emailRules" class="input" placeholder="请输入邮箱" style="padding-right: 40px" required solo />
                    <v-text-field v-model="form.nickname" :rules="usernameRules" class="input" placeholder="请输入昵称" style="padding-right: 40px" required solo />
                    <v-text-field
                        v-model="form.password"
                        :rules="passwordRules"
                        :append-icon="showPassword ? 'mdi-eye' : 'mdi-eye-off'"
                        @click:append="showPassword = !showPassword"
                        :type="showPassword ? 'text' : 'password'"
                        placeholder="请输入密码"
                        class="input"
                        required
                        solo
                    />
                    <v-text-field
                        v-model="form.passwordConfirm"
                        :rules="passwordConfirmRules"
                        :append-icon="showConfirmPassword ? 'mdi-eye' : 'mdi-eye-off'"
                        @click:append="showConfirmPassword = !showConfirmPassword"
                        :type="showConfirmPassword ? 'text' : 'password'"
                        placeholder="请确认密码"
                        class="input"
                        required
                        solo
                    />
                    <v-row align="center" justify="start" class="align-items-start">
                      <v-col cols="7" class="d-flex align-start">
                        <v-text-field
                            v-model="form.code"
                            :rules="codeRules"
                            placeholder="请输入验证码"
                            required
                            solo
                            class="code-input"
                            dense
                        />
                      </v-col>
                      <v-col cols="5" class="d-flex align-start">
                        <v-btn
                            @click="sendVerificationCode"
                            :disabled="isSendingCode"
                            block
                            :class="['codeget', { sending: isSendingCode }]"
                            height="39"
                        >
                          {{ isSendingCode ? `${countdown}s 后重试` : "获取验证码" }}
                        </v-btn>
                      </v-col>
                    </v-row>
                  </v-form>
                  <v-row>
                    <v-col lg="6">
                      <v-btn dark color="#340e0e" @click="register">注册</v-btn>
                    </v-col>
                    <v-col lg="6" style="text-align: right">
                      <v-btn text dark @click="toLogin">已有账号？去登录</v-btn>
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
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import request from '../utils/request'

const router = useRouter()
const formRef = ref(null)
const valid = ref(false)

const form = reactive({
  email: '',
  code: '',
  nickname: '',
  password: '',
  passwordConfirm: ''
})

const showPassword = ref(false)
const showConfirmPassword = ref(false)
const isSendingCode = ref(false)
const countdown = ref(60)

const usernameRules = [
  v => !!v || '请输入用户名',
  v => (v && v.trim().length !== 0) || '请输入用户名',
  v => (v && v.length <= 8) || '用户名长度不超过8个字符'
]

const passwordRules = [
  v => !!v || '请输入密码',
  v => (v && v.trim().length !== 0) || '请输入密码',
  v => (v && v.length <= 18) || '密码长度不超过18位',
  v => (v && v.length >= 8) || '密码长度不少于8位'
]

const passwordConfirmRules = [
  v => !!v || '请确认密码',
  v => v === form.password || '两次输入的密码不一致'
]

const emailRules = [
  v => !!v || '请输入邮箱',
  v => (v.includes('@') ? true : '邮箱必须包含@符号')
]

const codeRules = [
  v => !!v || '请输入验证码'
]

const register = async () => {
  formRef.value?.validate()
  if (valid.value) {
    try {
      const response = await request.post('/user/register', {
        email: form.email,
        password: form.password,
        nickname: form.nickname,
        code: form.code
      })
      if (response.code === '200') {
        alert('注册成功')
        await router.push('/login')
      } else {
        alert(response.msg)
      }
    } catch (error) {
      alert('注册失败，请稍后再试')
      console.error('注册请求出错:', error)
    }
  }
}

const toLogin = () => {
  router.push('/')
}

const sendVerificationCode = async () => {
  if (isSendingCode.value) return
  isSendingCode.value = true
  countdown.value = 60

  const timer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      clearInterval(timer)
      isSendingCode.value = false
    }
  }, 1000)

  try {
    await request.post('/user/sendCode', { email: form.email })
    // alert("验证码已发送，请检查邮箱")
  } catch (error) {
    alert('验证码发送失败，请稍后重试')
    console.error('验证码发送失败:', error)
    isSendingCode.value = false
  }
}
</script>

<style scoped>
.BG {
  height: 100vh;
  overflow-x: hidden;
  display: flex;
  background-image: url(../assets/img/page-header.png);
  margin:0px;
  background-size:100% 100%;
  background-attachment:fixed;
}

/* 最外层的大盒子 */
.box {
  width: 1050px;
  height: 600px;
  display: flex;
  /* 相对定位 */
  position: relative;
  z-index: 2;
  margin: auto;
  /* 设置圆角 */
  border-radius: 50px 50px 50px 50px;
  /* 设置边框 */
  border: 1px solid rgba(255, 255, 255, 0.6);
  /* 设置盒子阴影 */
  box-shadow: 2px 1px 19px rgba(0, 0, 0, 0.1);
}


.welcome{
  font-family:KaiTi;
  font-size:45px;
}

/* 滑动的盒子 */
.left-box {
  /* 宽度为大盒子的一半 */
  width: 50%;
  height: 100%;
  /* 绝对定位 */
  position: absolute;
  /* 距离大盒子左侧为0 */
  left: 0;
  /* 距离大盒子顶部为0 */
  top: 0;
  z-index: 99;
  border-radius: 50px 0px 0px 50px;
  background-color: #340e0e;
  box-shadow: 2px 1px 19px rgba(0, 0, 0, 0.1);

}

/* 滑动盒子的标题 */
.left-box h1 {
  margin-top: 150px;
  text-align: center;
  /* 文字间距 */
  letter-spacing: 5px;
  color: #eeeeee;
  /* 禁止选中 */
  user-select: none;
  /* 文字阴影 */
  text-shadow: 4px 4px 3px rgba(0, 0, 0, 0.1);
}

/* 滑动盒子的文字 */
.left-box p {
  height: 30px;
  line-height: 30px;
  text-align: center;
  margin: 20px 0;
  /* 禁止选中 */
  user-select: none;
  font-weight: bold;
  color: #eeeeee;
  text-shadow: 4px 4px 3px rgba(0, 0, 0, 0.1);
}

/* 图片盒子 */
.img-box {
  width: 300px;
  height: 250px;
  margin: 20px auto;
  /* 设置为圆形 */
  /*border-radius: 50%;*/
  /* 设置用户禁止选中 */
  user-select: none;
  overflow: hidden;
  /*box-shadow: 4px 4px 3px rgba(0, 0, 0, 0.1);*/
}

/* 图片 */
.img-box img {
  width: 100%
}

/* 登录盒子 */
.login-form {
  flex: 1;
  height: 100%;
  background-color: #4e1717;
  border-radius: 50px 50px 50px 50px;
}

/* 标题盒子 */
.title-box {
  margin-top: 40px;
  margin-bottom: 20px;
  text-align: center;
}

/* 标题 */
.title-box h1 {
  text-align: center;
  color: #e2e2e2;
  user-select: none;
  letter-spacing: 5px;
  text-shadow: 4px 4px 3px rgba(0, 0, 0, 0.1)
}
.form{
  height: 390px;
}
.codeget {
  background-color: #4CAF50; /* 设置背景色 */
  color: black; /* 设置文字颜色 */
  height: 45px; /* 设置高度 */
  font-size: 16px; /* 设置字体大小 */
  font-weight: bold; /* 设置字体加粗 */
  border-radius: 5px; /* 设置圆角 */
  border: none; /* 去掉边框 */
  transition: background-color 0.3s; /* 设置过渡效果 */
  width: 100%; /* 设置按钮的宽度为父容器宽度 */
  max-width: 150px; /* 限制按钮的最大宽度 */
  padding-top: 0; /* 去除按钮内边距上部，避免错位 */
  padding-bottom: 0; /* 去除按钮内边距下部，避免错位 */
  align-self: flex-start; /* 确保按钮顶部对齐 */
}

.codeget:disabled {
  background-color: #ccc; /* 禁用状态的背景色 */
  color: #666; /* 禁用状态的文字颜色 */
}

.codeget:hover {
  background-color: #DDAA00; /* 鼠标悬停时的背景色 */
}

.code-input{
  margin-top: 0; /* 保证输入框与按钮对齐 */
  padding-top: 0 !important; /* 去掉内部padding，避免错位 */
  width: 300px !important;
}

.d-flex {
  display: flex;
}

/* 新增调整样式 */
.align-items-start {
  align-items: flex-start !important;
}


.code-input .v-input__slot {
  margin: 0 !important; /* 移除默认边距 */

}
.codeget.sending {
  background-color: #DDAA00; /* 点击后的背景色 */
}
/* 修改点击后的样式（包含禁用状态） */
.codeget.sending:disabled {
  background-color: #DDAA00 !important; /* 强制覆盖 Vuetify 默认样式 */
  color: black !important; /* 保持文字颜色一致 */
}
.input{
  border-radius:6px;
  width:360px;
}
::v-deep .v-input__control {
  background-color: white !important;
  border-radius: 6px;
}
</style>
