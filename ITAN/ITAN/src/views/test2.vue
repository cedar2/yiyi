<template>
  <div class="whole-container">
    <!-- 顶部导航栏 -->
    <div class="top-navbar">
      <div class="navbar-content">
        <div class="navbar-logo">
          <img src="/assets/img/yj.png" alt="Logo" class="logo-image" />
          "易道生生"IXUE翻译助手
        </div>
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
                <v-btn @click="saveContent" :disabled="!isEditing" color="primary">
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
                <v-card-title class="bg-white">
                  翻译结果
                  <v-select
                      v-model="translationVersion"
                      :items="['大众版', '白话版', '学术版']"
                      class="ml-2"
                      dense
                      hide-details
                      style="max-width: 120px;"
                  ></v-select>
                </v-card-title>
                <v-card-text class="flex-grow-1 overflow-y-auto bg-white">
                  <div class="translation-content">
                    <p v-if="translationResult === ''">请先选择句读结果进行翻译</p>
                    <p v-else>{{ translationResult }}</p>
                  </div>
                </v-card-text>
                <v-card-actions class="bg-white">
                  <v-btn @click="copyTranslation" color="primary">
                    复制翻译
                  </v-btn>
                  <v-btn @click="exportTranslation" color="primary">
                    导出翻译
                  </v-btn>
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
    <v-dialog v-model="showDetailDialog" max-width="800px">
      <v-card>
        <v-card-title>
          句子详情
          <v-spacer></v-spacer>
          <v-btn icon @click="showDetailDialog = false">
            <v-icon>mdi-close</v-icon>
          </v-btn>
        </v-card-title>
        <v-card-text>
          <div class="sentence-detail">
            <div class="sentence-header">
              <h3>短句内容</h3>
              <p class="sentence-content">{{ selectedSentence }}</p>
            </div>
            <div class="word-explanations">
              <h3>字词解释</h3>
              <div class="word-list">
                <div
                    v-for="(word, index) in getWords(selectedSentence)"
                    :key="index"
                    class="word-item"
                >
                  <div class="word-text">{{ word }}</div>
                  <div class="word-meaning">
                    {{ getWordMeaning(word) || '暂无解释' }}
                  </div>
                </div>
              </div>
            </div>
            <div class="sentence-meaning">
              <h3>短句释义</h3>
              <p>{{ getSentenceMeaning(selectedSentence) || '暂无释义' }}</p>
            </div>
          </div>
        </v-card-text>
        <v-card-actions>
          <v-btn @click="showDetailDialog = false" color="primary">
            收起
          </v-btn>
          <v-btn @click="showTranslation" color="primary">
            翻译
          </v-btn>
        </v-card-actions>
      </v-card>
    </v-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElNotification } from 'element-plus'
import * as d3 from 'd3'
import request from "../utils/request"

const originalText = ref('')
// 句读结果
const segmentedText = ref('')
// 编辑状态
const isEditing = ref(false)
// 句读结果编辑状态
const isEditingSegmented = ref(false)


const showFullModules = ref(false)
const translationVersion = ref('大众版')
const selectedSentence = ref('')
const showDetailDialog = ref(false)
const translationResult = ref('')
const knowledgeData = ref(null)
const fileInput = ref(null)

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



// const handleFileUpload = async (event) => {
//   const file = event.target.files[0]
//   if (!file) return
//
//
//   const formData = new FormData()
//   formData.append('file', file);
//   formData.append('type', file.type.split('/')[1]) // 提取文件类型子类如pdf/docx等
// // 调试输出 FormData 内容
//   for (let [key, value] of formData.entries()) {
//     console.log(key, value);
//   }
//   // 发送请求
//   fetch('http://localhost:2728/upload', {
//     method: 'POST',
//     body: formData,  // 直接将 FormData 作为请求体
//   })
//       .then(response => {
//         if (!response.ok) {
//           throw new Error(`HTTP error! status: ${response.status}`);
//         }
//
//         return response.json(); // 解析服务器返回的 JSON 数据
//       })
//       .then(data => {
//         console.log('Success:', data);
//         originalText.value = data.content
//
//       })
//       .catch(error => {
//         console.error('Error:', error);
//         if (error.message.includes('Unexpected token')) {
//           console.error('Response body might be empty or not valid JSON.');
//         }
//       });
//   await saveContent()
// }

// 文本编辑
const toggleEdit = () => {
  isEditing.value = !isEditing.value
}
// const saveContent = () => {
//   return fetch('http://localhost:2728/parsing/sentence', {
//     method: 'POST',
//     headers: {
//       'Content-Type': 'application/json',
//     },
//     body: JSON.stringify({ text: originalText.value })
//   })
//       .then(async (response) => {
//         const text = await response.text();
//
//         // 调试输出原始响应
//         console.log('Raw Response:', text);
//
//         if (!response.ok) {
//           throw new Error(`HTTP ${response.status} | ${text.slice(0, 100)}`);
//         }
//
//         try {
//           return JSON.parse(text);
//         } catch (e) {
//           throw new Error(`JSON 解析失败: ${e.message}\n响应内容: ${text.slice(0, 200)}`);
//         }
//       })
//       .then(data => {
//         sentences.value = data;
//         isEditing.value = false;
//         return data; // 返回数据供后续使用
//       })
//       .catch(error => {
//         console.error('完整错误日志:', error);
//         throw error; // 保持错误传播
//       });
// }

// const saveContent = () => {
//   return fetch('http://localhost:2728/parsing/sentence', {
//     method: 'POST',
//     headers: {
//       'Content-Type': 'application/json',
//     },
//     body: JSON.stringify({ text: originalText.value })
//   })
//       .then(response => {
//         if (!response.ok) {
//           throw new Error(`HTTP error! status: ${response.status}`);
//         }
//         return response.json();
//       })
//       .then(data => {
//         sentences.value = data;
//         isEditing.value = false;
//       })
//       .catch(error => {
//         console.error('保存失败:', error);
//         throw error; // 保持错误冒泡以便后续处理
//       });
// }

const saveContent = async () => {
  try {
    const res = await request.post('http://localhost:2728/parsing/sentence', {
      text: originalText.value
    })
    // 打印整个响应对象
    console.log('完整响应:', res);

    // 打印响应的状态码
    console.log('响应状态码:', res.status);

    // 打印响应的状态消息
    console.log('响应状态消息:', res.statusText);

    // 打印响应的数据（通常是JSON）
    console.log('响应数据:', res.data);

    segmentedText.value = res.content
    isEditing.value = false

  } catch (error) {
    console.error('保存失败:', error)
  }
}

// 句读结果编辑
const toggleSegmentedEdit = () => {
  isEditingSegmented.value = !isEditingSegmented.value
}

const saveSegmentedText = () => {
  isEditingSegmented.value = false
  // 这里可以添加保存到后端的逻辑
}

// 处理句读文本点击事件
const handleSegmentedTextClick = (event) => {
  const target = event.target
  const selection = window.getSelection()
  const range = selection.getRangeAt(0)
  const startOffset = range.startOffset
  const text = target.value
  const sentences = text.split(/[，。！？；；]/).filter(s => s.trim() !== '')

  let clickedSentence = ''
  let currentPos = 0

  for (const sentence of sentences) {
    const sentenceLength = sentence.length
    if (startOffset >= currentPos && startOffset <= currentPos + sentenceLength) {
      clickedSentence = sentence
      break
    }
    currentPos += sentenceLength + 1 // 加上标点符号的长度
  }

  if (clickedSentence) {
    selectedSentence.value = clickedSentence
    showDetailDialog.value = true
  }
}

// 辅助函数
const getWords = (sentence) => {
  return sentence.split('')
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

// 翻译功能
const showTranslation = async () => {
  if (!selectedSentence.value) {
    ElNotification({
      title: '提示',
      message: '请先选择一个句子',
      type: 'warning'
    })
    return
  }

  showFullModules.value = true

  try {
    const res = await request.post('/translation', {
      text: selectedSentence.value,
      version: translationVersion.value
    })
    translationResult.value = res.data.result
    ElNotification({
      title: '翻译成功',
      message: '翻译结果已生成',
      type: 'success'
    })
  } catch (error) {
    ElNotification({
      title: '翻译失败',
      message: '翻译过程中出现错误',
      type: 'error'
    })
    console.error('翻译失败:', error)
  }

  // 加载知识图谱
  loadKnowledgeGraph()
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


const getWordMeaning = (word) => {
  // 模拟字词解释
  const meanings = {
    '易': '变易、变化',
    '经': '经典、经常',
    '道': '道路、方法',
    '生': '生长、产生',
    '天': '天空、自然',
    '地': '大地、土地',
    '人': '人类、人物'
  }
  return meanings[word] || ''
}

const getSentenceMeaning = (sentence) => {
  // 模拟句子释义
  const meanings = {
    '天行健，君子以自强不息': '天体运行刚健不息，君子应自觉奋发向上，永不松懈',
    '地势坤，君子以厚德载物': '大地的气势厚实和顺，君子应增厚美德，容载万物'
  }
  return meanings[sentence] || '暂无释义'
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

.empty-state {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100%;
  color: #999;
  font-size: 14px;
}

.sentence-list {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 10px 0;
}

.sentence-item {
  padding: 10px;
  border-radius: 4px;
  background-color: #f5f5f5;
  cursor: pointer;
  position: relative;
  transition: background-color 0.2s;
}

.sentence-item:hover {
  background-color: #e0e0e0;
}

.sentence-text {
  margin-right: 40px;
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
</style>
