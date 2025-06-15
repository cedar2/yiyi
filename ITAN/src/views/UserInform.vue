<template>
  <div class="form-wrapper">
    <el-form
        ref="formRef"
        :model="form"
        :rules="rules"
        label-width="100px"
        class="user-form"
    >
      <el-form-item label="昵称" prop="nickname">
        <el-input v-model="form.nickname" placeholder="请输入昵称" />
      </el-form-item>

      <el-form-item label="邮箱" prop="email">
        <el-input v-model="form.email" placeholder="请输入邮箱" />
      </el-form-item>

      <el-form-item label="密码" prop="password">
        <el-input
            v-model="form.password"
            type="password"
            placeholder="修改密码可选填"
            show-password
        />
      </el-form-item>

      <el-form-item label="确认密码" prop="confirmPassword">
        <el-input
            v-model="form.confirmPassword"
            type="password"
            placeholder="再次输入新密码"
            show-password
        />
      </el-form-item>

      <el-form-item>
        <el-button type="primary" @click="onSubmit">保存修改</el-button>
        <el-button @click="onReset">重置</el-button>
        <el-button type="info" @click="goHome">返回首页</el-button>
      </el-form-item>

    </el-form>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const formRef = ref()
import { useRouter } from 'vue-router'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
const form = reactive({
  nickname: '',
  email: '',
  password: '',
  confirmPassword: ''
})

const rules = {
  nickname: [
    { required: true, message: '昵称不能为空', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '邮箱不能为空', trigger: 'blur' },
    { type: 'email', message: '请输入有效的邮箱地址', trigger: 'blur' }
  ],
  password: [
    {
      validator(rule, value, callback) {
        if (value && value.length < 6) {
          callback(new Error('密码长度不能小于6位'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ],
  confirmPassword: [
    {
      validator(rule, value, callback) {
        if (form.password && value !== form.password) {
          callback(new Error('两次密码输入不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const onSubmit = () => {
  formRef.value.validate((valid) => {
    if (!valid) {
      ElMessage.error('请检查表单输入')
      return
    }

    const payload = {
      nickname: form.nickname,
      email: form.email,
      password: form.password
    }

    request.post('/user/updateInfo', payload)
        .then(res => {
          if (res.code === '200') {
            ElMessage.success('修改成功！')
          } else {
            ElMessage.error(res.msg || '修改失败')
          }
        })
        .catch(err => {
          console.error(err)
          ElMessage.error('请求出错')
        })
  })
}

const onReset = () => {
  formRef.value.resetFields()
}
</script>

<style scoped>
.form-wrapper {
  min-height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;

  background-image: url(@/assets/img/page-header.png);
  background-size: cover;
  background-attachment: fixed;
  background-position: center;
  padding: 40px;
}

.user-form {
  width: 100%;
  max-width: 500px;
  padding: 32px;
  border-radius: 16px;
  background-color: rgba(255, 255, 255, 0.95);
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.15);
}

/* 表单元素美化 */
::v-deep(.el-input__inner) {
  background-color: #ffffff;
  border-radius: 6px;
  padding: 10px;
}

::v-deep(.el-form-item__label) {
  color: #4e1717;
  font-weight: bold;
}

/* 按钮风格保持一致 */
.el-button--primary {
  background-color: #4e1717;
  border-color: #4e1717;
}

.el-button--primary:hover {
  background-color: #340e0e;
  border-color: #340e0e;
}

.el-button--info {
  background-color: #666;
  border-color: #666;
  color: #fff;
}
.el-button--info:hover {
  background-color: #444;
  border-color: #444;
}

</style>
