
<template>
  <div class="whole-container">
    <!-- 顶部导航栏 -->
    <div class="top-navbar">
      <div class="navbar-content">
        <div class="navbar-logo">
          <img src="/assets/img/yj.png" alt="Logo" class="logo-image" />
          "易道生生"IXUE翻译助手
        </div>
        <button class="send-button"  @click="upload">首页</button>
      </div>
    </div>

    <div class="page-container">
      <!-- 四个大模块 -->
      <v-container fluid class="fill-height content-wrapper">
        <v-row class="fill-height">
          <!-- 原文模块 -->
          <v-col cols="6">
            <v-card class="h-100 d-flex flex-column">
              <v-card-title class="bg-white">
                原文内容
                <v-btn @click="triggerFileInput" class="ml-2" small>上传文件</v-btn>
              </v-card-title>
              <v-card-text class="flex-grow-1 overflow-y-auto bg-white">
                <v-textarea
                    v-model="originalText"
                    placeholder="点击编辑输入内容/上传加载文献内容..."
                    :readonly="!isEditing"
                    auto-grow
                    variant="outlined"
                    hide-details
                    class="h-100"
                ></v-textarea>
              </v-card-text>
              <v-card-actions class="bg-white">
                <v-btn @click="toggleEdit" :color="isEditing ? 'primary' : ''">
                  {{ isEditing ? '取消编辑' : '编辑' }}
                </v-btn>
                <v-btn @click="saveContent" :disabled="!isEditing || !originalText?.trim()" color="primary">
                  保存
                </v-btn>
              </v-card-actions>
              <input
                  ref="fileInput"
                  type="file"
                  hidden
                  @change="handleFileUpload"
                  accept=".pdf,.doc,.docx,.txt,.jpg,.jpeg,.png"
              />
            </v-card>
          </v-col>

          <!-- 句读模块 -->
          <v-col cols="6">
            <v-card class="h-100 d-flex flex-column">
              <v-card-title class="bg-white">句读结果</v-card-title>
              <v-card-text class="flex-grow-1 overflow-y-auto bg-white">
                <v-textarea
                    v-model="segmentedText"
                    placeholder="句读结果将显示在这里"
                    :readonly="!isEditingSegmented"
                    auto-grow
                    variant="outlined"
                    hide-details
                    class="h-100"
                    @click="handleSegmentedTextClick"
                ></v-textarea>
              </v-card-text>
              <v-card-actions class="bg-white">
                <v-btn
                    @click="toggleSegmentedEdit"
                    :color="isEditingSegmented ? 'primary' : ''"
                >
                  {{ isEditingSegmented ? '取消编辑' : '编辑' }}
                </v-btn>
                <v-btn
                    @click="saveSegmentedText"
                    :disabled="!isEditingSegmented"
                    color="primary"
                >
                  保存句读结果
                </v-btn>
                <v-btn
                    @click="showTranslation"
                    color="primary"
                >
                  翻译
                </v-btn>
              </v-card-actions>
            </v-card>
          </v-col>

          <!-- 翻译和知识图谱模块 -->
          <template v-if="showFullModules">
            <v-col cols="6">
              <v-card class="h-100 d-flex flex-column">
                <v-card-title class="bg-white d-flex align-center py-1">
                  <span class="mr-2 font-weight-medium text--darken-1">翻译结果</span>
                  <v-select
                      v-model="translationVersion"
                      :items="['大众版', '白话版', '学术版']"
                      class="translation-select"
                      dense
                      hide-details
                      solo
                      flat
                  ></v-select>
                </v-card-title>


                <v-card-text class="flex-grow-1 overflow-y-auto bg-white position-relative">
                  <div class="translation-content" style="min-height: 100px; position: relative;">
                    <!-- 加载动画显示 -->
                    <div v-if="isLoading" class="loading-container">
                      <div class="spinner">
                        <i class="fas fa-circle-notch fa-spin"></i>
                      </div>
                      <div class="loading-text">翻译中，请稍候…</div>
                    </div>

                    <!-- 翻译结果显示 -->
                    <p v-else>{{ translationResult }}</p>
                  </div>
                </v-card-text>

                <v-card-actions class="bg-white">
                  <v-btn @click="copyTranslation" color="primary">复制翻译</v-btn>
                  <v-btn @click="exportTranslation" color="primary">导出翻译</v-btn>
                </v-card-actions>
              </v-card>
            </v-col>

            <v-col cols="6">
              <v-card class="h-100 d-flex flex-column">
                <v-card-title class="bg-white">知识图谱</v-card-title>
                <v-card-text class="flex-grow-1 overflow-y-auto bg-white">
                  <div id="knowledge-graph" class="graph-container"></div>
                </v-card-text>
                <v-card-actions class="bg-white">
                  <v-btn @click="expandGraph" color="primary">
                    展开图谱
                  </v-btn>
                  <v-btn @click="exportGraph" color="primary">
                    导出图谱
                  </v-btn>
                </v-card-actions>
              </v-card>
            </v-col>
          </template>
        </v-row>
      </v-container>
    </div>

    <!-- 句子详情弹窗 -->
    <v-dialog v-model="showDetailDialog" max-width="800px"class="dialog-content">
      <v-card class="detail-card">

        <v-card-text class="dialog-content">
          <div class="background-layer">
            <div class="sentence-detail">
              <!-- 短句内容区块 -->
              <div class="sentence-header glassmorphism">
                <h3 class="section-title">📜 短句内容</h3>
                <p class="sentence-content">{{ selectedSentence }}</p>
              </div>

              <!-- 字词解释区块 -->
              <div class="word-explanations glassmorphism">
                <h3 class="section-title">🔍 字词解析</h3>
                <div class="word-list">
                  <div
                      v-for="(word, index) in getWords(selectedSentence)"
                      :key="index"
                      class="word-item"
                  >
                    <div class="word-text">{{ word }}</div>
                    <div class="word-meaning">
                      {{ getWordMeaning(word) || '释义加载中...' }}
                    </div>
                  </div>
                </div>
              </div>

              <!-- 短句释义区块 -->
              <div class="sentence-meaning glassmorphism">
                <h3 class="section-title">📖 全句释义</h3>
                <p class="meaning-text">{{ getSentenceMeaning()|| '深度解析生成中...' }}</p>
              </div>
            </div>
          </div>

        </v-card-text>

        <v-card-actions class="dialog-actions">
          <v-btn
              @click="showDetailDialog = false"
              class="action-btn"
          >
            <v-icon left>mdi-chevron-up</v-icon>
            <span class="btn-text">收起</span>
          </v-btn>
          <v-btn
              @click="showTranslation"
              class="action-btn"
          >
            <v-icon left>mdi-translate</v-icon>
            <span class="btn-text">智能翻译</span>
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import {ref, computed, onMounted, watch} from 'vue'
import { ElNotification , ElLoading } from 'element-plus'
import * as d3 from 'd3'
import request from "../utils/request"
import { debounce } from 'lodash'
import router from "@/router/index.js";

//原文内容
const originalText = ref('')
// 句读结果
const segmentedText = ref('')
// 编辑状态
const isEditing = ref(false)
// 句读结果编辑状态
const isEditingSegmented = ref(false)
//句子详情句子翻译
const sentenceTranslations = ref(new Map()) // 存储句子翻译
const sentenceDataMap = ref({})
const currentProcessing = ref(new Set())
//翻译部分
const showFullModules = ref(false)
const translationVersion = ref('大众版')
const translationVersionLabelToValue = {
  '大众版': '1',
  '白话版': '2',
  '学术版': '3'
}
//句子详情弹窗
const selectedSentence = ref('')
const showDetailDialog = ref(false)
//全文的翻译结果
const translationResult = ref('')

const knowledgeData = ref(null)
const fileInput = ref(null)

let pollingInterval = null
let loadingInstance = null
const isLoading = ref(false)
//返回首页
const upload = () => {
  router.push('/homeView')
}



// 文件上传
const triggerFileInput = () => fileInput.value.click()

const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', file.type.split('/')[1])

  try {
    const response = await fetch('http://localhost:2728/upload', {
      method: 'POST',
      body: formData,
    })
    const data = await response.json()
    originalText.value = data.content
    await saveContent()
  } catch (error) {
    console.error('文件上传失败:', error)
  }
}




// 文本编辑
const toggleEdit = () => {
  isEditing.value = !isEditing.value
}


// 句读处理（异步处理）
const saveContent = async () => {
  try {
    const res = await request.post('http://localhost:2728/parsing/sentence', {
      text: originalText.value
    })
    console.log('进程id', res.ID);
    startPolling(res.ID);
  } catch (error) {
    console.error('保存失败:', error)
  }
}
const startPolling = (id) => {
  const interval = setInterval(async () => {
    const  data  = await request.get(`http://localhost:2728/check/${id}`);
    console.log('状态', data.task_status);
    if (data.task_status === 'DONE') {
      clearInterval(interval);
      console.log('内容', data.result);
      segmentedText.value = data.result
      isEditing.value = false
    }
  }, 2000);
};

// 句读结果编辑
const toggleSegmentedEdit = () => {
  isEditingSegmented.value = !isEditingSegmented.value
}

const saveSegmentedText = () => {
  isEditingSegmented.value = false
  // 这里可以添加保存到后端的逻辑
}

// 句子详情弹框：处理句读文本点击事件（精确位置版本）
const handleSegmentedTextClick = (event) => {
  if (isEditingSegmented.value) return

  const target = event.target
  const oriText = target.value

  //  替换换行符为临时标记
  const processedText = oriText.replace(/\n/g, '↵')

  // 获取点击精确位置（基于原始文本坐标）
  const clickPos = target.selectionStart

  // 创建句子位置映射表
  const sentenceMap = []
  let currentIndex = 0

  // 使用处理后的文本进行分割
  const tempSegments = processedText.split(/([，。！？；])/).filter(s => s)

  // 重组句子结构
  let sentenceAcc = ""
  tempSegments.forEach((segment) => {
    if (/^[，。！？；]$/.test(segment)) {
      const fullSentence = sentenceAcc + segment
      const startPos = currentIndex - sentenceAcc.length
      const endPos = currentIndex + segment.length

      sentenceMap.push({
        text: fullSentence,
        start: startPos,
        end: endPos
      })

      sentenceAcc = ""
    } else {
      sentenceAcc += segment
    }
    currentIndex += segment.length
  })

  // 处理最后未闭合的句子
  if (sentenceAcc) {
    sentenceMap.push({
      text: sentenceAcc,
      start: currentIndex - sentenceAcc.length,
      end: currentIndex
    })
  }

  // 查找匹配的句子
  const matched = sentenceMap.find(s =>
      clickPos >= s.start &&
      clickPos < s.end
  )

  if (matched) {

    selectedSentence.value = matched.text.replace(/↵/g, '').replace(/[，。！？；]+$/, '')//+$/ 匹配连续出现的末尾标点
    showDetailDialog.value = true
  }
}

// 辅助函数
const getWords = (sentence) => {
  return sentence.split('')
}

// 弹窗单句翻译方法
const getSentenceMeaning = () => {
  return sentenceTranslations.value.get(selectedSentence.value) || '翻译加载中...'
}


const debouncedFetchSentenceTranslation = debounce(() => {
  fetchSentenceTranslation()
}, 10000) // 10000 毫秒内多次调用只执行一次

watch(translationVersion, () => {
  debouncedFetchSentenceTranslation()
})


// 监听弹窗打开事件
watch(() => showDetailDialog.value, async (newVal) => {
  if (newVal && selectedSentence.value) {
    // 检查是否已经有翻译结果，如果有则直接展示，否则调用接口
    if (!sentenceTranslations.value.has(selectedSentence.value)) {
      await fetchSentenceTranslation()
    }
  }
})

// 获取弹窗句子翻译
const fetchSentenceTranslation = async () => {


  // 优先使用全局翻译，根据标点符号对应，可能有问题，之后再进行优化
  if (translationResult.value) {
    const sourceSentences = segmentedText.value.split(/[，。！？；]/)
    const targetSentences = translationResult.value.split(/[,.!?;]/)

    const index = sourceSentences.indexOf(selectedSentence.value)
    if (index !== -1 && targetSentences[index]) {
      sentenceTranslations.value.set(
          selectedSentence.value,
          targetSentences[index]
      )
      return
    }
  }
  // 独立翻译请求
  try {
    const data = await request.post('http://localhost:2728/translation/translate', {
      text: selectedSentence.value,
      translationType:translationVersionLabelToValue[translationVersion.value]

  })

    const translation = await pollTranslationResult(data.ID)
    sentenceTranslations.value.set(selectedSentence.value, translation)
  } catch (error) {
    console.error('独立翻译失败:', error)
    ElNotification.warning('局部翻译加载失败')
  }
}

// 单句翻译改进的轮询方法
const pollTranslationResult = (Id,  timeout = 30000) => {
  return new Promise((resolve, reject) => {
    const startTime = Date.now()

    const check = async () => {
      try {
        const data  = await request.get(`http://localhost:2728/check/${Id}`)

        if (data.task_status === 'DONE') {
          resolve(data.result)
        } else if (Date.now() - startTime < timeout) {
          setTimeout(check, 10000)
        } else {
          reject(new Error('翻译超时'))
        }
      } catch (error) {
        reject(error)
      }
    }

    check()
  })
}








// 句子编辑
const editSentence = (index) => {
  isEditingSentence.value = index
  editedSentence.value = sentences.value[index]
}

const saveSentence = (index) => {
  sentences.value[index] = editedSentence.value
  isEditingSentence.value = -1
}

const cancelEditSentence = () => {
  isEditingSentence.value = -1
}

const saveAllSentences = async () => {
  try {
    await request.post('/parsing/sentence/save', {
      sentences: sentences.value
    })
    ElNotification({
      title: '保存成功',
      message: '句读结果已保存',
      type: 'success'
    })
  } catch (error) {
    ElNotification({
      title: '保存失败',
      message: '句读结果保存失败',
      type: 'error'
    })
    console.error('保存失败:', error)
  }
}

// 句子详情
const showSentenceDetail = (sentence) => {
  selectedSentence.value = sentence
  showDetailDialog.value = true
}



const debouncedShowTranslation = debounce(() => {
  showTranslation()
}, 500) // 500 毫秒内多次调用只执行一次

watch(translationVersion, () => {
  debouncedShowTranslation()
})


// 翻译功能(异步处理）
const showTranslation = async () => {
  console.log(segmentedText.value)
  if (!segmentedText.value) {
    ElNotification({
      title: '提示',
      message: '请先选择一个句子',
      type: 'warning'
    })
    return
  }

  showFullModules.value = true
  isLoading.value = true
  console.log(translationVersion.value)

  try {
    const payload = ({
      text: segmentedText.value,
      translationType: translationVersionLabelToValue[translationVersion.value]
    })

    const res = await request.post('http://localhost:2728/translation/translate', payload)
    console.log('进程id', res.ID);
    startPollingTranslation(res.ID);

    // ElNotification({
    //   title: '翻译成功',
    //   message: '翻译结果已生成',
    //   type: 'success'
    // })
  } catch (error) {
    // ElNotification({
    //   title: '翻译失败',
    //   message: '翻译过程中出现错误',
    //   type: 'error'
    // })
    console.error('翻译失败:', error)
  }

  loadKnowledgeGraph()
}
const startPollingTranslation = (id) => {
  const interval = setInterval(async () => {
    const data = await request.get(`http://localhost:2728/check/${id}`)
    if (data.task_status === 'DONE') {
      clearInterval(interval)
      translationResult.value = data.result
      isEditing.value = false
      isLoading.value = false
    }
  }, 2000)
}
// 知识图谱
const loadKnowledgeGraph = () => {
  // 模拟知识图谱数据
  const graphData = {
    nodes: [
      { id: 1, name: '易经', group: 1 },
      { id: 2, name: '八卦', group: 1 },
      { id: 3, name: '乾卦', group: 2 },
      { id: 4, name: '坤卦', group: 2 },
      { id: 5, name: '哲学', group: 3 },
      { id: 6, name: '道家', group: 3 }
    ],
    links: [
      { source: 1, target: 2, value: 1 },
      { source: 1, target: 3, value: 1 },
      { source: 1, target: 4, value: 1 },
      { source: 2, target: 3, value: 1 },
      { source: 2, target: 4, value: 1 },
      { source: 1, target: 5, value: 1 },
      { source: 1, target: 6, value: 1 }
    ]
  }

  knowledgeData.value = graphData

  // 使用D3.js绘制知识图谱
  const width = 600
  const height = 400

  const svg = d3.select('#knowledge-graph')
      .html('')
      .append('svg')
      .attr('width', width)
      .attr('height', height)

  const color = d3.scaleOrdinal(d3.schemeCategory10)

  const simulation = d3.forceSimulation()
      .force('link', d3.forceLink().id(d => d.id))
      .force('charge', d3.forceManyBody())
      .force('center', d3.forceCenter(width / 2, height / 2))

  const link = svg.append('g')
      .selectAll('line')
      .data(graphData.links)
      .enter().append('line')
      .attr('stroke-width', d => Math.sqrt(d.value))

  const node = svg.append('g')
      .selectAll('circle')
      .data(graphData.nodes)
      .enter().append('circle')
      .attr('r', 8)
      .attr('fill', d => color(d.group))
      .call(d3.drag()
          .on('start', dragstarted)
          .on('drag', dragged)
          .on('end', dragended))

  node.append('title')
      .text(d => d.name)

  simulation
      .nodes(graphData.nodes)
      .on('tick', ticked)

  simulation.force('link')
      .links(graphData.links)

  function ticked() {
    link
        .attr('x1', d => d.source.x)
        .attr('y1', d => d.source.y)
        .attr('x2', d => d.target.x)
        .attr('y2', d => d.target.y)

    node
        .attr('cx', d => d.x)
        .attr('cy', d => d.y)
  }

  function dragstarted(event, d) {
    if (!event.active) simulation.alphaTarget(0.3).restart()
    d.fx = d.x
    d.fy = d.y
  }

  function dragged(event, d) {
    d.fx = event.x
    d.fy = event.y
  }

  function dragended(event, d) {
    if (!event.active) simulation.alphaTarget(0)
    d.fx = null
    d.fy = null
  }
}

const expandGraph = () => {
  // 模拟扩展图谱
  ElNotification({
    title: '图谱扩展',
    message: '正在加载更多节点和关系',
    type: 'info'
  })
  // 实际项目中这里应该调用后端接口获取更多数据
}

const exportGraph = () => {
  ElNotification({
    title: '图谱导出',
    message: '图谱已导出为JSON格式',
    type: 'success'
  })
  // 实际项目中这里应该调用后端接口导出图谱
}


// 字词解释方法
const getWordMeaning = (word) => {
  const data = sentenceDataMap.value[selectedSentence.value]
  if (!data?.entities) return ''

  // 精确匹配优先
  const exactMatch = data.entities.find(e => e.name === word)
  if (exactMatch) return exactMatch.explanation

  // 包含匹配备选
  const containsMatch = data.entities.find(e => e.name.includes(word))
  return containsMatch ? containsMatch.explanation : ''
}



// 翻译相关
const copyTranslation = () => {
  navigator.clipboard.writeText(translationResult.value).then(() => {
    ElNotification({
      title: '复制成功',
      message: '翻译内容已复制到剪贴板',
      type: 'success'
    })
  }, () => {
    ElNotification({
      title: '复制失败',
      message: '复制过程中出现错误',
      type: 'error'
    })
  })
}

const exportTranslation = () => {
  const blob = new Blob([translationResult.value], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '翻译结果.txt'
  a.click()
  URL.revokeObjectURL(url)
  ElNotification({
    title: '导出成功',
    message: '翻译结果已导出为文本文件',
    type: 'success'
  })
}
</script>

<style scoped>
/* 顶部导航栏样式 */
.top-navbar {
  width: 100%;
  background-color: #480000;
  padding: 20px 20px;
  position: fixed;
  top: 0;
  left: 0;
  z-index: 1000;
  color: white;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.1);
}

.navbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.navbar-logo {
  font-size: 24px;
  font-weight: bold;
  display: flex;
  align-items: center;
}

.logo-image {
  width: 24px;
  height: 24px;
  margin-right: 8px;
}

.navbar-links {
  display: flex;
  list-style: none;
  margin: 0;
  padding: 0;
}

.navbar-links li {
  margin-left: 20px;
  cursor: pointer;
  font-size: 16px;
}

.navbar-links a {
  color: white;
  text-decoration: none;
}

.navbar-links a:hover {
  color: #FFD700;
  transform: scale(1.1);
}

.page-container {
  position: relative;
  height: 100vh;
  overflow: auto;
  background-color: #f9f9f9;
}

.content-wrapper {
  z-index: 1;
  padding-top: 64px;
}

.h-100 {
  height: 100%;
}

.overflow-y-auto {
  overflow-y: auto;
  max-height: calc(100% - 120px);
}

.fill-height {
  height: calc(100vh - 64px - 2rem);
  margin-top: 30px;
  min-height: 90px;
}


.save-btn, .cancel-btn {
  position: absolute;
  right: 10px;
  top: 10px;
}

.edit-input {
  position: absolute;
  right: 40px;
  top: 10px;
  width: calc(100% - 80px);
}

.sentence-detail {
  padding: 20px;
}

.sentence-header, .word-explanations, .sentence-meaning {
  margin-bottom: 20px;
}

.sentence-content {
  background-color: #f0f0f0;
  padding: 10px;
  border-radius: 4px;
  margin-top: 10px;
}

.word-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  gap: 15px;
  margin-top: 10px;
}

.word-item {
  background-color: #f5f5f5;
  padding: 10px;
  border-radius: 4px;
}

.word-text {
  font-weight: bold;
  margin-bottom: 5px;
}

.word-meaning {
  font-size: 12px;
  color: #666;
}

.graph-container {
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.translation-content {
  padding: 10px;
  line-height: 1.6;
}

/*翻译美化样式*/
.loading-container {
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, 0.92);
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  z-index: 10;
  border-radius: 12px;
  animation: fadeIn 0.3s ease;
}

.spinner i {
  font-size: 32px;
  color: #409EFF;
  animation: spin 1s infinite linear;
}

.loading-text {
  margin-top: 8px;
  font-size: 14px;
  color: #666;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

@keyframes fadeIn {
  from { opacity: 0; }
  to { opacity: 1; }
}


/* 返回主页按钮美化 */
.send-button {
  padding: 10px 20px;
  background-color: #480000;
  color: white;
  border: none;
  border-radius: 50px;
  margin-left: 10px;
  cursor: pointer;
  transition: background-color 0.3s ease, transform 0.1s ease;
}

.send-button:hover {
  background-color: #3B0000;
  transform: scale(1.05);
}

.send-button:disabled {
  background-color: #999999;
  cursor: not-allowed;
}




/* 毛玻璃效果 */
.glassmorphism {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

/* 句子弹窗部分样式 */

/* 主色调系 */
:root {
  --primary-dark: #300000;
  --primary-main: #480000;
  --primary-light: #600000;
  --accent-gold: #c5a173;
}

/* 背景渐变层 */
.background-layer {
  background: linear-gradient(135deg, #c7bfbf 0%, #522d2d 100%);
  border-radius: 8px;
  padding: 16px;
}

/* 毛玻璃效果 */
.glassmorphism {
  background: rgba(255, 245, 245, 0.92);
  backdrop-filter: blur(8px);
  border: 1px solid rgba(72, 0, 0, 0.1);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 4px 6px rgba(72, 0, 0, 0.08);
}

/* 标题样式 */
.section-title {
  color: var(--primary-main);
  border-left: 4px solid var(--accent-gold);
  padding-left: 12px;
  margin-bottom: 16px;
  font-size: 1.1rem;
  text-shadow: 1px 1px 2px rgba(218, 216, 216, 0.5);
}

/* 短句内容 */
.sentence-content {
  font-size: 1.3rem;
  line-height: 1.6;
  color: var(--primary-dark);
  text-align: center;
  margin: 20px 0;
  font-weight: 500;
}

/* 字词列表 */
.word-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 12px;
}

.word-item {
  background: rgba(255, 255, 255, 0.95);
  padding: 12px;
  border-radius: 8px;
  border: 1px solid #f0dada;
  transition: all 0.3s;
}

.word-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 3px 6px rgba(72, 0, 0, 0.1);
}

.word-text {
  color: var(--primary-main);
  font-weight: bold;
}

.word-meaning {
  color: #664444;
  font-size: 0.9rem;
}

/* 全句释义 */
.meaning-text {
  color: #4d2a2a;
  line-height: 1.8;
  font-size: 1rem;
}

/* 卡片整体样式 */
.detail-card {
  border: 2px solid var(--primary-main);
  box-shadow: 0 10px 20px rgba(72, 0, 0, 0.2) !important;
}



.action-btn {
  display: inline-flex !important;
  align-items: center;
  justify-content: center;
  min-width: 100px !important;  /* 确保最小宽度 */
  height: 40px !important;      /* 固定高度 */
  padding: 0 20px !important;   /* 左右对称留白 */
}

.action-btn .v-icon {
  margin-right: 8px !important;  /* 图标右侧间距 */
  margin-left: -4px !important;  /* 补偿Vuetify默认偏移 */
}

.btn-text {
  color: #bb1013;
  line-height: 1;          /* 消除行高影响 */
  transform: translateY(1px); /* 微调垂直对齐 */
  letter-spacing: 0.5px;   /* 优化文字间距 */
}

/* 悬停状态优化 */
.action-btn:hover {
  transform: scale(1.05) translateZ(0); /* 启用GPU加速 */
  box-shadow: 0 4px 12px rgba(72, 0, 0, 0.2);
}
/* 自定义下拉框样式 */
.translation-select {
  max-width: 200px; /* 根据最长选项调整 */
  margin-left: 8px;

  /* 覆盖默认样式 */
  ::v-deep .v-input__control {
    min-height: 28px !important;
  }

  ::v-deep .v-input__slot {
    background: transparent !important;
    box-shadow: none !important;
    padding: 0 8px !important;
    border: 1px solid #e0e0e0;
    border-radius: 4px;
    transition: all 0.3s;
  }

  ::v-deep .v-select__selection {
    font-size: 13px;
    color: #480000;
    font-weight: 500;
  }

  ::v-deep .v-icon {
    color: #480000 !important;
    font-size: 16px;
  }

  /* 悬停状态 */
  ::v-deep .v-input__slot:hover {
    border-color: #c5a173;
    box-shadow: 0 2px 6px rgba(72, 0, 0, 0.08);
  }

  /* 聚焦状态 */
  ::v-deep .v-input--is-focused .v-input__slot {
    border-color: #480000;
    box-shadow: 0 2px 8px rgba(72, 0, 0, 0.1) !important;
  }
}


</style>
