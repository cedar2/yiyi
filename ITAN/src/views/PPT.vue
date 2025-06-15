<template>

  <!-- 自定义导航按钮 -->
  <div class="page_navigate">
    <div
        id="page_creator"
        :class="{ selected: currentPage === 'creator' }"
        @click="toPage('creator')"
    >
      生成PPT
    </div>
    <div
        id="page_dashboard"
        :class="{ selected: currentPage === 'dashboard' }"
        @click="toPage('dashboard')"
    >
      PPT列表
    </div>
    <div
        id="page_customTemplate"
        :class="{ selected: currentPage === 'customTemplate' }"
        @click="toPage('customTemplate')"
    >
      自定义模板
    </div>
    <div @click="toBook">
      返回古籍处理系统
    </div>
  </div>

  <!-- iframe 容器 -->
  <div id="container" ref="containerRef"></div>
  <!-- 展示用户原文/翻译结果的悬浮面板 -->
  <div class="data-panel" :class="{ open: showPanel }">
    <div class="panel-toggle" @click="showPanel = !showPanel">
      {{ showPanel ? '收起内容' : '展开内容' }}
    </div>
    <div v-if="showPanel" class="panel-content">
      <el-collapse v-model="activeSections">
        <el-collapse-item name="original" title="原文">
          <div class="text-content">{{ store.originalText }}</div>
        </el-collapse-item>
        <el-collapse-item name="segmented" title="句读">
          <div class="text-content">{{ store.segmentedText }}</div>
        </el-collapse-item>
        <el-collapse-item name="translations" title="翻译结果">
          <div class="text-content">
            <p><strong>大众版：</strong>{{ translationResults['大众版'] }}</p>
            <p><strong>白话版：</strong>{{ translationResults['白话版'] }}</p>
            <p><strong>学术版：</strong>{{ translationResults['学术版'] }}</p>
          </div>
        </el-collapse-item>
      </el-collapse>
    </div>
  </div>

</template>

<script setup>
import { onMounted, ref } from 'vue'

// API 参数配置
const apiKey = 'ak_R_1MPfEEvrFrrOvJma' // TODO 填写你的 API-KEY
const uid = 'xxx'
const limit = null

const containerRef = ref(null)
const currentPage = ref('creator')
const token = ref('') // 响应式 token
let docmeeUI = null

function createApiToken(apiKey, uid, limit) {
  if (!apiKey) {
    alert('请在代码中设置 apiKey')
    return ''
  }
  const xhr = new XMLHttpRequest()
  xhr.open('POST', 'https://docmee.cn/api/user/createApiToken', false)
  xhr.setRequestHeader('Api-Key', apiKey)
  xhr.setRequestHeader('Content-Type', 'application/json')
  xhr.send(JSON.stringify({ uid, limit }))
  if (xhr.status === 200) {
    const resp = JSON.parse(xhr.responseText)
    if (resp.code !== 0) {
      alert('创建token异常：' + resp.message)
      return ''
    }
    return resp.data.token
  } else {
    alert('网络异常, httpStatus: ' + xhr.status)
    return ''
  }
}

function toPage(page) {
  docmeeUI.navigate({ page })
  currentPage.value = page
}

// 初始化
onMounted(() => {
  if (location.protocol === 'file:') {
    alert('不支持 file 协议直接访问，请启动 http 服务访问！\n\n启动命令：npm run start')
  }

  token.value = createApiToken(apiKey, uid, limit)

  if (!token.value || !containerRef.value) return

  // 初始化 DocmeeUI
  docmeeUI = new window.DocmeeUI({
    pptId: null,
    token: token.value,
    container: containerRef.value,
    page: currentPage.value,
    lang: 'zh',
    mode: 'light',
    isMobile: false,
    background: 'linear-gradient(-157deg,#f57bb0, #867dea)',
    padding: '40px 20px 0px',
    onMessage(message) {
      console.log('message', message)

      if (message.type === 'invalid-token') {
        const newToken = createApiToken(apiKey, uid, limit)
        docmeeUI.updateToken(newToken)
      } else if (message.type === 'beforeGenerate') {
        const { subtype, fields } = message.data
        if (subtype === 'outline') {
          console.log('即将生成ppt大纲', fields)
          return true
        } else if (subtype === 'ppt') {
          console.log('即将生成ppt', fields)
          docmeeUI.sendMessage({ type: 'success', content: '继续生成PPT' })
          return true
        }
      } else if (message.type === 'beforeCreateCustomTemplate') {
        const { file, totalPptCount } = message.data
        if (totalPptCount < 2) {
          console.log('用户生成积分不足，不允许制作自定义完整模版')
          return false
        }
        return true
      } else if (message.type === 'pageChange') {
        currentPage.value = message.data.page
      } else if (message.type === 'beforeDownload') {
        const { subject } = message.data
        return `PPT_${subject}.pptx`
      } else if (message.type === 'error') {
        if (message.data.code === 88) {
          alert('您的次数已用完')
        } else {
          alert('发生错误：' + message.data.message)
        }
      }
    }
  })
})

//选择原文、句读、翻译
import { useTextStore } from '@/stores/useTextStore'
import {useRouter} from "vue-router";
import { storeToRefs } from 'pinia'

const store = useTextStore()
const { translationResults } = storeToRefs(store)

const showPanel = ref(false)

const activeSections = ref(['original', 'segmented', 'translations'])
const router = useRouter()
const toBook= () => {
  router.push("/book")
}
</script>

<style scoped>
/* 保证页面满屏且不滚动 */
html,
body,
#app {
  margin: 0;
  padding: 0;
  width: 100vw;
  height: 100vh;
}

/* 顶部提示信息样式 */
.top-tip {
  text-align: center;
  padding: 10px;
  font-size: 14px;
  background-color: #f9f9f9;
  color: #333;
  line-height: 1.6;
}
.top-tip a {
  color: #f57bb0;
  text-decoration: none;
  font-weight: bold;
}

/* 页面导航样式 */
.page_navigate {
  padding: 5px;
  height: 50px;
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 10px;
}
.page_navigate > div {
  padding: 8px 16px;
  cursor: pointer;
  user-select: none;
  border: 2px solid #f0f0f0;
  border-radius: 6px;
  background: #fff;
  transition: background 0.2s;
}
.page_navigate > div:hover {
  background: #f5f5f5;
}
.page_navigate .selected {
  background: #f57bb0;
  color: white;
}

/* iframe 容器样式 */
#container {
  flex: 1;
  margin: 0 auto;
  width: calc(100% - 50px);
  height: calc(100vh - 120px); /* 动态适配剩余空间 */
  border-radius: 12px;
  box-shadow: 0 0 12px rgba(120, 120, 120, 0.3);
  overflow: hidden;
  background: linear-gradient(-157deg, #f57bb0, #867dea);
  color: white;
}
/* 新增选择原文句读翻译*/
.data-panel {
  position: fixed;
  bottom: 20px;
  right: 20px;
  width: 360px;
  max-height: 80vh;
  background: white;
  color: #333;
  border-radius: 12px;
  box-shadow: 0 0 12px rgba(0, 0, 0, 0.15);
  overflow: hidden;
  z-index: 999;
  transition: transform 0.3s ease;
  transform: translateY(calc(100% - 40px));
  display: flex;
  flex-direction: column;
}
.data-panel.open {
  transform: translateY(0);
}
.panel-toggle {
  background: #f57bb0;
  color: white;
  text-align: center;
  padding: 10px;
  border-radius: 12px 12px 0 0;
  cursor: pointer;
  font-weight: bold;
}
.panel-content {
  padding: 10px;
  overflow-y: auto;
  flex-grow: 1;
}
.text-content {
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-word;
  max-height: 200px;
  overflow-y: auto;
}
</style>

