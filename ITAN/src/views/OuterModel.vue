<template>
  <div class="chat-container">
    <!-- 顶部导航栏 -->
    <div class="top-navbar">
      <div class="navbar-content">
        <div class="navbar-logo">
          AI对话助手
        </div>
        <div class="navbar-user-info">
          <span class="nickname">{{ nickname }}</span>
        </div>
      </div>
    </div>
    <!-- 左侧功能栏 -->
    <div class="sidebar ancient-style" :class="{ 'collapsed': !showAside }">
      <!-- 折叠按钮，始终悬浮在侧边 -->
      <div
          class="sider-toggle-button"
          :class="showAside ? 'from-aside' : 'from-left'"
          @click="toggleAside"
      >
        <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 25 25" class="icon">
          <path
              fill="currentColor"
              fill-rule="evenodd"
              :d="showAside ? arrowLeft : arrowRight"
              clip-rule="evenodd"
          />
        </svg>
      </div>

      <!-- 展开的内容 -->
      <div v-if="showAside" class="sidebar-content">
        <ul class="catalog-list">
          <li class="catalog-item level-1" @click="startNewConversation">
            <div class="item-header">
              <i class="fas fa-history"></i>
              <span class="item-label">新建对话</span>
            </div>
          </li>
          <ChatHistoryList :selectedId="selectedConversationId" @select="selectConversation" />
        </ul>
      </div>
    </div>

    <div class="background-container">
      <div class="chat-messages horizontal-layout"
           ref="chatMessagesRef">
        <div
            v-for="(message, index) in messages"
            :key="index"
            class="message"
            :class="[message.type === 'bot' ? message.layoutClass : '', message.type]"
        >
          <!-- 机器人消息 -->
          <template v-if="message.type === 'bot'">
            <div class="rating-avatar-container">
              <div class="avatar-container">
                <img :src="message.avatar" :alt="message.type" class="avatar" />
              </div>
              <div class="rating">
                <span class="rating-label">回答评分：</span>
                <span v-if="!message.rating">
                  <span
                      v-for="n in 5"
                      :key="n"
                      class="star"
                      :class="{ 'filled': message.hoverRating !== null && n <= message.hoverRating }"
                      @click="rateMessage(message, n, message.model)"
                      @mouseover="message.hoverRating = n"
                      @mouseleave="message.hoverRating = null"
                  >★</span>
                </span>
                <span v-else class="stars-with-rating">
                <span v-for="n in 5" :key="n" class="star" :class="{'filled': n <= message.rating}">★</span>
                <span class="rating-score">{{ message.rating }}</span>
              </span>
              </div>
            </div>

            <!-- 机器人文本内容-->
            <div
                class="bot-message"
                v-if="message.text && message.text.length > 0"
            >
              <template v-if="message.model === 'local' || message.model === 'local32B' || message.model === 'local32BnoRAG'">
                <span v-for="(chunk, index) in message.text" :key="index" class="response-span">
                  {{ chunk }}
                </span>
              </template>
              <span v-else v-html="message.text"></span>
              <!--              <div class="timestamp">{{ formatTimestamp(message.timestamp) }}</div>-->
            </div>
            <!-- 播放语音按钮 -->
            <div class="voice-button-container"
                 v-if="message.text && (typeof message.text === 'string' || message.text.length) && !message.d3Code">
              <button @click="speakText(message)" class="voice-button">
                <span v-if="!message.isSpeaking">📢 听语音</span>
                <span v-else-if="message.isPaused">▶️ 继续</span>
                <span v-else>⏸ 暂停</span>
              </button>

              <button
                  @click="stopSpeech(message)"
                  class="voice-button"
                  v-if="message.isSpeaking"
                  style="margin-left: 8px;">
                ⏹ 停止
              </button>
            </div>

            <!-- 如果存在 d3Code，则挂载渲染图像的容器 -->
            <div
                v-if="showDiagram"
                :id="`diagram-${message.id}`"
                class="diagram-container"
                style="margin-top: 12px;"
            ></div>
          </template>


          <!-- 用户消息 -->
          <template v-else>
            <div class="user-message-container">
              <div class="user-message">
                <span v-html="message.text"></span>
                <!--                <div class="timestamp">{{ formatTimestamp(message.timestamp) }}</div>-->
              </div>
              <div class="avatar-container">
                <img :src="message.avatar" :alt="message.type" class="avatar" />
              </div>
            </div>
          </template>

        </div>
        <div v-if="(paintDiagram && !showDiagram) || isTyping"  class="typing-indicator">正在思考，请稍等...</div>
        <!--        <div ref="chartRef" class="chart-box" v-show="chartVisible"></div>-->
      </div>


      <div v-if="showModelTip" class="model-tip">
        <i class="fas fa-info-circle"></i> 请选择你需要的大模型（最多可选三个模型）<br>你可以对不同大模型的回答进行打分
      </div>
      <div class="graph-button-container">
        <el-button
            @click="showGraphData"
            class="load-btn"
            :disabled="buttonDisabled">
          显示知识图谱
        </el-button>

        <el-dialog
            v-model="chartVisible"
            title="知识图谱"
            width="70%"
            top="5vh"
            :destroy-on-close="false"
            @opened="onDialogOpened"
            @closed="onDialogClosed"
        >
          <div ref="chartRef" class="chart-box" style="height:600px;"></div>
        </el-dialog>
      </div>
      <div class="model-selector-container">
        <button
            class="model-select-button"
            :class="{ 'selected-model': selectedModels.includes('local') }"
            @click="toggleModelMenu"
        >
          <i class="fas fa-robot"></i> 选择模型 ▼
        </button>
        <div v-show="isModelMenuOpen" class="model-menu">
          <div
              v-for="model in modelOptions"
              :key="model.value"
              class="model-menu-item"
              :class="{'selected': selectedModels.includes(model.value), 'disabled': !selectedModels.includes(model.value) && selectedModels.length >= 3}"

              @click="selectModel(model.value)"
          >
            {{ model.label }}
          </div>
        </div>
      </div>

      <div class="chat-input-container">
        <div class="input-box">
          <input v-model="userInput" type="text" @keyup.enter="sendMessageAll" placeholder="请输入您要问的问题"/>
        </div>
        <button @click="sendMessageAll" class="send-button" :disabled="(paintDiagram && !showDiagram)||isSendingMessage">发送</button>
        <button class="send-button" :disabled="(paintDiagram && !showDiagram)||isSendingMessage" @click="upload">返回首页</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount, watch } from 'vue'
import { useRouter } from 'vue-router'
import request from "../utils/request"
import { reactive, nextTick } from 'vue'
import ChatHistoryList from "@/views/ChatHistoryList.vue";

const router = useRouter()

// 静态资源引入
import userAvatar from '../../public/assets/img/touxiang.jpg'
import botAvatar from '../../public/assets/img/yitan.png'
import bot32BAvatar from '../../public/assets/img/bagua.png'
import bot32BAvatarnoRAG from '../../public/assets/img/upup.png'
import deepseekAvatar from '../../public/assets/img/touxinag_deepseek.png'
import kimiAvatar from '../../public/assets/img/touxiang_kimi.png'
import eventBus from '@/utils/eventBus'
import * as echarts from "echarts";

// 响应式状态
// const apiKey = ref("sk-aWheLOkwpDLcjDsmT05PNriib45SgFohvJUvUNk0wWZuwAyR")
// const apiKey_DeepSeek = ref("sk-a0bc3d1bb1cd4a88aceb5a7bbcea243a")
// const apiKey_DeepSeek = ref(import.meta.env.VITE_DEEPSEEK_API_KEY);

const nickname = ref(localStorage.getItem('nickname') || '暂未登录')
const isNearBottom = ref(true)
const userInput = ref('')
const messages = ref([])
const isTyping = ref(false)
// const isSidebarCollapsed = ref(false)
const selectedModels = ref(['local'])
const isModelMenuOpen = ref(false)
const showModelTip = ref(true)
const isSendingMessage = ref(false)
const responseData = ref([])
const chatMessagesRef = ref(null)
const graphDataMap = ref(new Map()) // 存储每个问题的图谱数据
const currentQuestion = ref('')     // 当前问题标识
const buttonDisabled = ref(true); // 初始禁用
const showDiagram = ref(false);  // 初始象数图画占位为空
const paintDiagram = ref(false);  // 初始象数图画状态
let UserQues = ref('')
let modelId;
let question =ref('')
let currentConversationId = null

const chartRef = ref(null); // 绑定 DOM 容器
let chartInstance = null
let chartVisible = ref(false); // 控制显示

//语音播报
const isSpeaking = ref(false)
const isPausedRef = ref(false)

let utterance = null
let currentUtterance = null
let currentSpeakingMessageId = null

watch(
    () => messages.value,
    () => {
      nextTick(() => {
        checkScrollPosition() // 先检查当前位置
        scrollToBottom()     // 根据位置决定是否滚动
      })
    },
    { deep: true }
)

const checkScrollPosition = () => {
  if (!chatMessagesRef.value) return

  const { scrollTop, scrollHeight, clientHeight } = chatMessagesRef.value
  const distanceFromBottom = scrollHeight - (scrollTop + clientHeight)
  isNearBottom.value = distanceFromBottom < 100  // 距离底部小于100px视为在底部区域
}
// 3. 滚动方法
const scrollToBottom = () => {
  if (isNearBottom.value && chatMessagesRef.value) {
    // console.log("滚动到底部！");
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight
  }
}

onMounted(() => {
  if (chatMessagesRef.value) {
    chatMessagesRef.value.addEventListener('scroll', checkScrollPosition)
  }
})

onBeforeUnmount(() => {
  if (chatMessagesRef.value) {
    chatMessagesRef.value.removeEventListener('scroll', checkScrollPosition)
  }
})


const modelOptions = ref([
  {label: 'IWEN', value: 'local'},
  // {label: 'IWEN(32B)', value: 'local32B'},
  // {label: 'IWEN(32B)noRAG', value: 'local32BnoRAG'},
  {label: 'DeepSeek', value: 'deepseek'},
  {label: 'Kimi', value: 'kimi'}
])

const upload = () => {
  router.push('/')
}


const goToFeedback = () => {
  router.push('/feedback')
}

const rateMessage = (message, rating, modelName) => {
  message.rating = rating
  const scoreData = {}

  // 只更新对应模型的分数
  switch (modelName) {
    case 'local': scoreData.score1 = rating; break
    case 'local32B': scoreData.score4 = rating; break
    case 'local32BnoRAG': scoreData.score5 = rating; break
    case 'kimi': scoreData.score2 = rating; break
    case 'deepseek': scoreData.score3 = rating; break
  }

  const dataToSend = {
    id: modelId,
    userMessage: question,
    selectedModels: selectedModels.value,
    ...scoreData
  }

  sendScoreToBackend(dataToSend)
}


const startNewConversation = () => {
  selectedConversationId.value = null //清除选中状态
  currentConversationId = null
  messages.value = []
  eventBus.emit('refresh-history')

}


const sendMessageAll = async () => {
  if (!userInput.value.trim()) return

  // 更新当前问题并保留旧问题的数据
  const newQuestion = userInput.value.trim()

  // 如果是新问题则清除当前问题的图谱缓存
  if (newQuestion !== currentQuestion.value) {
    currentQuestion.value = newQuestion
    graphDataMap.value.delete(currentQuestion.value)
  }

  await fetchGraphData()

  //console.log(localStorage.getItem('userId'))
  question = newQuestion
  userInput.value = ''

  messages.value.push({
    text: question,
    type: 'user',
    avatar: userAvatar,
    // timestamp: new Date()
  })

  const count = selectedModels.value.length
  const layoutClass = count === 2 ? 'two-models'
      : count >= 3 ? 'three-models'
          : 'one-model'

  // 获取提示词
  // const prompt = generatePrompt(question)
  const prompt = question
  console.log("conID",currentConversationId)
  try {
    isSendingMessage.value = true
    isTyping.value = true
    showModelTip.value = false

    console.log("conID2",currentConversationId)

    if (!currentConversationId) {
      const newConvId = await createNewConversation({
        userid:localStorage.getItem('userId'),
        discribe: question
      })
      currentConversationId = newConvId
      console.log(currentConversationId,'*****')
    }
    console.log("模型"+selectedModels.value)
    // 判断绘画

    
    const handlers = selectedModels.value.map(model =>
        createMessageHandler(model, prompt, layoutClass)
    )

    const responses = await Promise.all(handlers.map(fn => fn()))
    console.log("回答",responses)
    console.log(selectedModels.value)

    const modelResponses = selectedModels.value.map((model, index) => {
      const response = responses[index]
      return {
        modelName: model,
        botResponse: response.text,
        ...(response.d3Code ? { d3Code: response.d3Code } : {})
      }
    })

    //聚合后发送给后端
    const backendData = {
      conversation: currentConversationId,
      selectedModels: [...selectedModels.value],
      userMessage: question,
      modelResponses: modelResponses
    }

    const responseData=await sendMessageToBackend(backendData)
    if (responseData) {
      modelId = responseData;
      console.log('modelId:',modelId);
    } else {
      console.error('后端返回的数据没有包含 id');
    }
  } catch (e) {
    console.error('全局错误:', e)
  } finally {
    isSendingMessage.value = false
    isTyping.value = false
  }

}

const createMessageHandler = (modelType, prompt, layoutClass) => {
  return async () => {
    const avatar = updateAvatar(modelType)
    let response
    showDiagram.value = false;  // 初始象数图画占位为空
    paintDiagram.value = false;  // 初始象数图画状态
    const botMessage = reactive({
      id: Date.now() + Math.random(),
      model: modelType,
      text: '正在思考，请稍后...',
      avatar: avatar,
      type: 'bot',
      layoutClass: layoutClass,
      // timestamp: new Date(),
      rating: null,
      hoverRating: null,
      isSpeaking: false,  //
      isPaused: false
    })
    messages.value.push(botMessage)
    const id = botMessage.id;

    try {
       response = await sendRequestByModel(modelType, prompt)
       if (!response.ok || !response.body) throw new Error("响应失败或未接收到流数据")
       
      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''
      let fullText = ''
      botMessage.text = ''

      if (modelType === 'deepseek') {
        let drawStarted = false; // 是否已经开始绘图
        let checkDrawPromise
        checkDrawPromise = fetch('/api/DrawPromise', {
          method: 'POST',
          headers: { 'Content-Type': 'application/json' },
          body: JSON.stringify({ text: prompt })
        })
          .then(res => res.json())
          .then(data => {
            if (data.shouldDraw === 'true' && !drawStarted) {
              paintDiagram.value = !paintDiagram.value;
              drawStarted = true;
              console.log("开始绘图");
              fetch('/api/Draw', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ content: prompt })
              })
                .then(res => res.json())
                .then(data => {
                  const seq = data.seq;
                  console.log(seq);
                  seq.forEach(char => console.log(char));
                  paintGua(id,seq);
                })
                .catch(drawErr => {
                  showDiagram.value = false;    
                  paintDiagram.value = false;   
                  console.error('绘图请求出错:', drawErr);           
                });
            }
          })
          .catch(err => {
            console.error('检查是否需要绘图时出错:', err);
          });
        }

       // 接收流数据
      while (true) {
        const { done, value } = await reader.read()
        if (done) break
        buffer += decoder.decode(value, { stream: true })
        const chunks = buffer.split('\n')
        buffer = chunks.pop() || ''

        for (const chunk of chunks) {
          const trimmed = chunk.trim()
          if (!trimmed) continue
          let content = extractStreamContent(trimmed, modelType)
          if (content) {
            botMessage.text += content
            fullText += content
            await nextTick()
          }
        }
      }

      // if (true) {
      //   // 调用renderD3SVG提取并渲染SVG，传入完整文本
      //   // const d3Code = await renderD3SVG(botMessage, fullText)
      //   const d3Code = ''
      //   // console.log("提取 d3code：", d3Code)

      //   // 提取除<D3JS>代码外的文字作为描述
      //   const description = fullText.replace(/<D3JS>[\s\S]*<\/D3JS>/, '').trim()

      //   // 更新botMessage的text和d3Code字段，确保UI响应式更新
      //   botMessage.text = description || "这是根据您请求绘制的卦象图示。"
      //   botMessage.d3Code = d3Code || null

      //   // 返回同样数据，方便调用处继续处理
      //   return {
      //     text: botMessage.text,
      //     d3Code: botMessage.d3Code
      //   }
      // }
      return {
        text: botMessage.text
      }
    } catch (err) {
      console.error(`${modelType}请求失败:`, err)
      const idx = messages.value.findIndex(m => m.id === botMessage.id)
      if (idx !== -1) messages.value[idx].text = `请求失败: ${err.message}`
    }
  }
}

const paintGua = async (id, seq) => {
  showDiagram.value = !showDiagram.value;
  await nextTick();

  const targetId = `diagram-${id}`;
  const container = document.getElementById(targetId);
  console.log("容器", container);

  if (!container) {
    console.warn("找不到容器:", targetId);
    return;
  }

  // 清除旧图形
  d3.select(container).select("svg").remove();

  // 获取容器宽度，自适应
  const containerWidth = container.clientWidth || 600;
  const svgWidth = containerWidth;
  const svgHeight = 450;

  const lineHeight = 30;
  const lineGap = 70;
  const baseY = 50 + (5 * lineGap); 

  const svg = d3.select(container)
    .append("svg")
    .attr("width", svgWidth)
    .attr("height", svgHeight);
// 动画前立即滚动，让用户看到动画
  await new Promise((resolve) => requestAnimationFrame(resolve));
  await nextTick(); // 再等一帧
  if (chatMessagesRef.value) {
    chatMessagesRef.value.scrollTop = chatMessagesRef.value.scrollHeight;
    console.log("立即滚动到底，让用户看到动画");
  }
  for (let i = 0; i < 6; i++) {
    const y = baseY - i * lineGap;
    const delay = i * 400; 

    if (seq[i] === '阳') {
      svg.append("rect")
        .attr("x", svgWidth / 2 - 100)
        .attr("y", y)
        .attr("width", 0)
        .attr("height", lineHeight)
        .attr("fill", "#000")
        .transition()
        .duration(500)
        .delay(delay)
        .attr("width", 200);
    } else {
      // 阴爻左边
      svg.append("rect")
        .attr("x", svgWidth / 2 - 100)
        .attr("y", y)
        .attr("width", 0)
        .attr("height", lineHeight)
        .attr("fill", "#888")
        .transition()
        .duration(500)
        .delay(delay)
        .attr("width", 80);

      // 阴爻右边
      svg.append("rect")
        .attr("x", svgWidth / 2 + 20)
        .attr("y", y)
        .attr("width", 0)
        .attr("height", lineHeight)
        .attr("fill", "#888")
        .transition()
        .duration(500)
        .delay(delay)
        .attr("width", 80);
    }
  }
};

// 请求大模型
const sendRequestByModel = async (modelType, prompt) => {
  switch (modelType) {
    case 'local':
      return await fetch("/qwen14b", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({question: prompt})
      })
    case 'local32B':
      return await fetch("/qwen32b", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({question: prompt})
      })
    case 'local32BnoRAG':
      return await fetch("/qwen32b_SFT", {
        method: "POST",
        headers: {"Content-Type": "application/json"},
        body: JSON.stringify({question: prompt})
      })
    case 'deepseek':
      return await fetch(`api/deepStream?question=${encodeURIComponent(prompt)}`, {
            method: 'GET',
            headers: {
              'Accept': 'text/event-stream',  // 明确告诉服务器我们希望接收 SSE 格式（可选）
            },
          })
    case 'kimi':
          return await fetch(`api/kimiStream?question=${encodeURIComponent(prompt)}`, {
            method: 'GET',
            headers: {
              'Accept': 'text/event-stream',  // 明确告诉服务器我们希望接收 SSE 格式（可选）
            },
          })
    default:
      throw new Error("未知模型")
  }
}

const extractStreamContent = (chunk, modelType) => {
  if (!chunk.startsWith('data: ')) return ''
  const payload = chunk.replace('data: ', '')

  if (payload === '[DONE]') return ''

  if (modelType === 'deepseek' || modelType === 'kimi') {
    const data = JSON.parse(payload);
    const base64Content = data.choices?.[0]?.delta?.content ||'';
    return base64Content ? new TextDecoder().decode(Uint8Array.from(atob(base64Content), c => c.charCodeAt(0))) : '';
  }

  if (modelType === 'local' || modelType === 'local32B' || modelType === 'local32BnoRAG') {
    return payload.replace(/\\n/g, '\n')
  } 
}


// const generatePrompt = (question) => {
//   // 判断是否绘制卦象
//   // const isGuaRequest = /(错卦|综卦|变卦|卦象|爻)/.test(question)
//   //const isGuaRequest = /(nothing)/.test(question)
//   if (!isGuaRequest.value) 
//   return question;
//   console.log(question+"周易")
//   return `你是一个周易大师和代码大师，请严格按照以下要求生成 简短的描述和SVG + D3.js 绘图代码，用于渲染指定卦象：

// 1. 首行简短描述卦象（1-2句），不要任何多余解释。
// 2. 紧接着输出 D3.js 绘图代码，代码必须用 <D3JS> 和 </D3JS> 标签包裹。
// 3. 绘图代码要求：
//  根据用户提供的卦象内容绘制图形，例如“比卦的错卦”表示将比卦中所有阳爻变成阴爻，阴爻变成阳爻，生成天火同人卦（䷌）。
//  每一爻使用 <rect> 元素表示，尺寸严格为宽 80px、高 20px。
//  阳爻：一整条横线，fill="#000"，stroke="#000"。
//  阴爻：左右两段短横线，中间断开，各长 30px，间距 20px，fill="#888"，stroke="#888"。
//  每条爻垂直排列，竖直间距为 30px，从上至下依次绘制（第六爻在最上方）。
//  SVG 容器大小固定为 500px × 400px。
//  所有元素必须直接添加在 svg 根节点中，禁止使用 <g> 标签或任何嵌套结构。

// 示例输入："比卦的错卦"

// 示例输出：
// 比卦（水地䷇）的错卦是将其所有爻阴阳反转，生成天火同人卦（䷌），包含下卦坤变乾、上卦坎变离的双重变化，具体绘制如下：

// <D3JS>
// ...D3.js代码...
// </D3JS>

// 请严格只输出上述格式内容，保证正确性，一定要保证文字描述与生成的代码一致，不要添加任何多余解释、道歉、背景介绍或无关文字。`
// }

// const renderD3SVG = async (botMessage, fullText) => {
//   const d3CodeRaw = extractD3Code(fullText)
//   console.log("流式拼接结果：", fullText)
//   console.log("提取的 d3CodeRaw：", d3CodeRaw)

//   if (!d3CodeRaw) {
//     botMessage.text += '\n（未识别到卦象图形代码）'
//     return
//   }

//   botMessage.d3Code = d3CodeRaw
//   await nextTick()

//   const container = document.getElementById(`diagram-${botMessage.id}`)
//   console.log("", container)
//   if (!container) return

//   container.innerHTML = ''
//   const d3Code = d3CodeRaw
//       .replace(/(document\.body|document\.documentElement|d3\.select\((['"])body\2\))/g, 'container')
//       .replace(/d3\.select\((['"])\#?\w+\1\)/g, 'd3.select(container)')
//       .replace(/(\.append\()(['"]svg['"])\)/g, '$1$2)')
//       .replace(/d3\.create\(["']svg["']\)/g, 'd3.select(container).append("svg")') // 处理 d3.create
//       .replace(/\.node\(\)\.appendChild\(([\w.]+)\)/g, '') // 移除 .node().appendChild(...)
//       .replace(/document\.(querySelector|getElementById)\([^)]+\)/g, 'container') // 处理其他选择器形式
//       .replace(/(\.append\(['"]svg['"]\))\s*\.attr\((['"])width\2,\s*\d+\)/g, '$1.attr("width", 500)')
//       .replace(/(\.append\(['"]svg['"]\))\s*\.attr\((['"])height\2,\s*\d+\)/g, '$1.attr("height", 400)')
//       .replace(/container\.appendChild\(.*?\.node\(\)\);?/g, '') // 移除 container.appendChild(svg.node())
//       .replace(/const\s+container\s*=\s*d3\.select\(\s*container\s*\);?/g, '') // 移除 const container = d3.select(container)
//       .replace(/\bcontainer\.append\(/g, 'd3.select(container).append('); // ✅ 修复 container.append 报错


//   console.log("最终处理代码：", d3Code);
//   renderDiagram(d3Code, container)

//   return d3Code
// }

// 从回答中提取d3代码
// const extractD3Code = (text) => {
//   const startTag = "<D3JS>"
//   const endTag = "</D3JS>"
//   const start = text.indexOf(startTag)
//   const end = text.indexOf(endTag)
//   if (start !== -1 && end !== -1 && end > start) {
//     return text.slice(start + startTag.length, end).trim()
//   }
//   return null
// }



// function renderDiagram(code, container) {
//   if (!window.d3) {
//     console.error("D3.js 未加载")
//     return
//   }

//   if (!container) {
//     console.error("找不到 SVG 容器")
//     return
//   }

//   try {
//     container.innerHTML = '';

//     const renderFn = new Function('d3', 'container', `
//       (function() {
//         const __SAFE_CONTAINER__ = container;
//         try {
//           ${code
//         .replace(/document\./g, '__SAFE_CONTAINER__/.') // 防止 document 污染
//         .replace(/d3\.select\((['"])[^'"]+?\1\)/g, 'd3.select(__SAFE_CONTAINER__)')
//     }
//         } catch(e) {
//           const errorBox = document.createElement('div');
//           errorBox.innerHTML = '<p style="color:red">渲染错误：</p><pre>' + e.message + '</pre>';
//           __SAFE_CONTAINER__.appendChild(errorBox);
//         }
//       })()
//     `);

//     renderFn(d3, container);
//   } catch (e) {
//     console.error("图形渲染失败:", e);
//     const errorMsg = document.createElement('div');
//     errorMsg.textContent = "图形渲染失败，请检查 D3.js 代码格式。";
//     errorMsg.style.color = 'red';
//     container.appendChild(errorMsg);
//   }
// }


const createNewConversation = (data) => {
  console.log(data);  // 打印数据，确保格式正确
  return request.post('/conversation/addConversation', data)
      .then(response => {
        console.log('创建对话成功:', response);
        console.log(response.data.id); // 打印成功的响应
        return response.data.id;  // 返回数据
      })
      .catch(error => {
        console.error('创建对话失败:', error);
        throw error;  // 如果失败，抛出错误
      });
}

const sendMessageToBackend = (data) => {
  console.log(data);  // 打印数据，确保格式正确
  return request.post('/chatqa/addChatqa', data)
      .then(response => {
        console.log('消息发送成功:', response);  // 打印成功的响应
        return response.data;  // 返回数据
      })
      .catch(error => {
        console.error('发送消息到后端失败:', error);
        throw error;  // 如果失败，抛出错误
      });
}

const sendScoreToBackend = async (data) => {
  try {
    console.log('发送的数据:', JSON.stringify(data));
    console.log('发送的数据:',data);
    const response = await request.put('/chatqa/updateScores', data, {
      headers: {'Content-Type': 'application/json'}
    });
    console.log('评分数据发送成功:', response.data); // 打印返回的数据
  } catch (error) {
    console.error('发送评分数据到后端失败:', error);
  }
}


const toggleModelMenu = () => {
  if (!isModelMenuOpen.value) {
    isModelMenuOpen.value = !isModelMenuOpen.value
  }
}

const selectModel = (value) => {
  const index = selectedModels.value.indexOf(value)
  if (index === -1) {
    selectedModels.value.push(value)
  } else {
    selectedModels.value.splice(index, 1)
  }
}

const updateAvatar = (model) => {
  switch (model) {
    case 'deepseek':
      return deepseekAvatar
    case 'kimi':
      return kimiAvatar
    case 'local32B':
      return bot32BAvatar
    case 'local32BnoRAG':
      return bot32BAvatarnoRAG
    default:
      return botAvatar
  }
}

//左侧功能栏
//
// const toggleSidebar = () => {
//   isSidebarCollapsed.value = !isSidebarCollapsed.value
// }
const showAside = ref(true)

const toggleAside = () => {
  showAside.value = !showAside.value
}

// 箭头 SVG 路径
const arrowLeft =
    'M15.377 6.444a.5.5 0 0 1 0 .708l-5.293 5.292a.5.5 0 0 0 0 .707l5.293 5.293a.5.5 0 0 1 0 .707l-.354.354a.5.5 0 0 1-.707 0L8.14 13.328a.75.75 0 0 1 0-1.06l6.176-6.177a.5.5 0 0 1 .707 0z'

const arrowRight =
    'M9.623 6.444a.5.5 0 0 0 0 .708l5.293 5.292a.5.5 0 0 1 0 .707l-5.293 5.293a.5.5 0 0 0 0 .707l.354.354a.5.5 0 0 0 .707 0l6.176-6.177a.75.75 0 0 0 0-1.06L10.684 6.444a.5.5 0 0 0-.707 0z'


// 生命周期
onMounted(() => {
  const clickHandler = (e) => {
    const container = document.querySelector('.model-selector-container')
    if (container && !container.contains(e.target)) {
      isModelMenuOpen.value = false
    }
  }
  document.addEventListener('click', clickHandler)


  onBeforeUnmount(() => {
    document.removeEventListener('click', clickHandler)
  })


})

onMounted(() => {
  nickname.value = localStorage.getItem('nickname') || '暂未登录'
})
onMounted(() => {
  console.log(currentConversationId)
})
//
// const logout = () => {
//   localStorage.removeItem('token')
//   localStorage.removeItem('userId')
//   localStorage.removeItem('nickname')
//   router.push('/')
// }
const selectedConversationId = ref(null)//新增
const selectConversation = ({ id, userMessage, fullMessages }) => {
  selectedConversationId.value = id//新增
  messages.value = [] // 清空原消息
  showModelTip.value = false
  currentConversationId=localStorage.getItem('convID')
  fullMessages.forEach((msg, index) => {
    if (msg.role === 'user') {
      messages.value.push({
        id: `user-${index}`,
        type: 'user',
        text: msg.content,
        avatar: userAvatar,
        // timestamp: new Date()
      })
    } else {
      messages.value.push({
        id: `${msg.role}-${index}`,
        type: 'bot',
        text: msg.content,
        avatar: updateAvatar(msg.role),
        model: msg.role,
        layoutClass: msg.modelCount,
        // timestamp: new Date(),
        rating: msg.score
      })
    }
  })

  nextTick(() => {
    scrollToBottom()
  })
}

const showGraphData = async () => {

  //buttonDisabled.value = false;
  chartVisible.value = true
}
const fetchGraphData = async () => {
  try {
    // 如果已有缓存数据直接使用
    // if (graphDataMap.value.has(currentQuestion.value)) {
    //   chartVisible.value = true
    //   return
    // }

    const response = await fetch('/getGraph', {
      method: 'POST',
      headers: {'Content-Type': 'application/json'},
      body: JSON.stringify({ question: currentQuestion.value }),
    })

    const result = await response.json()

    chartVisible.value = false
    buttonDisabled.value = false;
    // 存储图谱数据
    graphDataMap.value.set(currentQuestion.value, {
      nodes: processNodes(result),
      links: processLinks(result),
      categories: processCategories(result)
    })

    //chartVisible.value = true
  } catch (error) {
    console.error('获取图谱数据失败:', error)
  }
}

// 新增数据处理方法
const processNodes = (result) => {
  const entities = result.entities
  const add_entities = result.add_entities
  const combined_entities = [...entities, ...add_entities]

  const typeSet = [...new Set(entities.map(item => item[2]))]
  const colors = [
    '#FF6666', '#3399FF', '#66CC99', '#9966CC', '#FFCC00',
    '#FF9966', '#66CCCC', '#CC99FF', '#FF6699', '#99CC33'
  ]

  const categories = typeSet.map((item, index) => ({
    name: item,
    itemStyle: { color: colors[index % colors.length] },
  }))

  categories.push({
    name: '未知',
    itemStyle: { color: '#808080' }
  })

  return combined_entities.map(item => ({
    id: parseInt(item[0]),
    name: item[1],
    symbolSize: item[2] === '未知' ? 30 : 50,
    category: item[2] === '未知' ? categories.length - 1 : typeSet.indexOf(item[2]),
    tooltip: {
      //formatter: `{b}<br/>类型: {c}`,
      // 原始详细格式：
      formatter: `<div style="width:300px;white-space:normal">
        <strong>${item[1]}</strong><br/>
        类型: ${item[2]}<br/>
        描述: ${item[3]}
      </div>`
    },
    label: {
      show: item[2] !== '未知',
    }
  }))
}

const processLinks = (result) => {
  return result.relationships.map(item => ({
    source: parseInt(item[7]),
    target: parseInt(item[8]),
    label: item[4].length < 7 ? item[4] : '',
  }))
}
const processCategories = (result) => {
  const typeSet = [...new Set(result.entities.map(item => item[2]))]
  const colors = [
    '#FF6666', '#3399FF', '#66CC99', '#9966CC', '#FFCC00',
    '#FF9966', '#66CCCC', '#CC99FF', '#FF6699', '#99CC33'
  ]

  return [
    ...typeSet.map((item, index) => ({
      name: item,
      itemStyle: { color: colors[index % colors.length] }
    })),
    { name: '未知', itemStyle: { color: '#808080' } }
  ]
}

// 完整的弹窗处理方法
const onDialogOpened = () => {

  nextTick(() => {
    if (!chartRef.value) {
      console.error('图表容器未找到！');
      return;
    }
    if (!chartInstance) {
      chartInstance = echarts.init(chartRef.value);
    }
    // 获取当前问题的数据
    const currentData = graphDataMap.value.get(currentQuestion.value)

    if (currentData) {
      const option = {
        //title: { text: '知识图谱', left: 'center' },
        tooltip: {
          formatter: (params) => {
            return params.dataType === 'edge'
                ? `关系: ${params.data.label}`
                : `实体: ${params.data.name}`
          }
        },
        legend: [{
          data: currentData.categories.map(c => c.name),
          //selectedMode: 'single'
        }],
        series: [{
          type: 'graph',
          layout: 'force',
          symbolSize: 50,
          roam: true,
          label: { show: true },
          edgeLabel: {
            show: true,
            // formatter: '{@label}'
            formatter: (x) => {
              const label = x.data.label || ''
              return label.length < 7 ? label : ''
            }
          },
          data: currentData.nodes,
          links: currentData.links,
          categories: currentData.categories,
          force: {
            repulsion: 200,
            edgeLength: [80, 200]
          }
        }]
      }

      chartInstance.clear()
      chartInstance.setOption(option)
      chartInstance.resize()

    }
  });

}

//语音播报
const speakText = (message) => {
  const text = Array.isArray(message.text) ? message.text.join('') : message.text
  if (!text) return

  // 当前是这条消息正在播放，切换暂停/继续
  if (message.isSpeaking) {
    if (speechSynthesis.paused) {
      speechSynthesis.resume()
      message.isPaused = false
    } else {
      speechSynthesis.pause()
      message.isPaused = true
    }
    return
  }

  // 正在播放其他消息内容
  speechSynthesis.cancel()
  resetAllSpeechStatus()

  const newUtterance = new SpeechSynthesisUtterance(text)
  newUtterance.lang = 'zh-CN'
  newUtterance.rate = 1
  newUtterance.pitch = 1

  newUtterance.onend = () => {
    message.isSpeaking = false
    message.isPaused = false
    currentSpeakingMessageId = null
  }

  message.isSpeaking = true
  message.isPaused = false
  currentUtterance = newUtterance
  currentSpeakingMessageId = message.id

  speechSynthesis.speak(newUtterance)
}

const stopSpeech = (message) => {
  speechSynthesis.cancel()
  message.isSpeaking = false
  message.isPaused = false
  currentSpeakingMessageId = null
}
const resetAllSpeechStatus = () => {
  if (currentSpeakingMessageId) {
    const msg = messages.value.find(m => m.id === currentSpeakingMessageId)
    if (msg) {
      msg.isSpeaking = false
      msg.isPaused = false
    }
  }
}


</script>

<style scoped>
/* 整体聊天容器样式 */
body::-webkit-scrollbar {
  display: none;
}

body {
  -ms-overflow-style: none;  /* IE 和 Edge */
  scrollbar-width: none;     /* Firefox */
}

.chat-container {
  display: flex;
  flex-direction: row;
  height: 100vh;
  background-color: #f0f2f5;
  transition: background-color 0.3s;
  opacity: 0;
  animation: fadeIn 1s forwards;
}


/* 顶部导航栏样式 */
.top-navbar {
  height: 60px;
  line-height: 60px;
  background: linear-gradient(90deg, #f3e9e4, #fff8f5); /* 浅色渐变背景 */
  color: #5b3a33; /* 深棕色文字 */
  display: flex;
  padding: 0 24px;
  font-family: 'Noto Serif SC', serif;
  font-weight: 700;
  font-size: 20px;
  box-shadow: 0 2px 8px rgba(91, 58, 51, 0.1); /* 柔和阴影 */
  user-select: none;
  z-index: 20;
  border-bottom: 1px solid #d9cfc7; /* 浅色边框 */
  backdrop-filter: saturate(180%) blur(4px);
  -webkit-backdrop-filter: saturate(180%) blur(4px);
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
}

.navbar-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
  width: 100%;
}

.navbar-logo {
  display: flex;
  align-items: center;
  gap: 12px; /* 新增间距 */
}

.logo-image {
  width: 32px;  /* 调大尺寸 */
  height: 32px;
  object-fit: contain;
}

.navbar-user-info {
  display: flex;
  align-items: center;
  gap: 20px;  /* 增大间距 */
}

/* 昵称 */
.nickname {
  font-size: 16px;
  font-weight: 600;
  color: #5b3a33; /* 同步深棕色 */
  padding: 4px 12px;
  border-radius: 8px;
  background: rgba(255, 255, 255, 0.3);
}

/* 左侧功能栏样式 */
.sidebar.ancient-style {
  width: 250px;
  display: flex;
  flex-direction: column;
  transition: width 0.3s ease;
  overflow: hidden;
  margin-top: 70px;

  /* 古典风纯色背景 */
  background-color: #f9f4ec;

  /* 移除背景图片 */
  background-image: none;

  /* 古风边框与内阴影 */
  border-right: 2px solid #c9a27d;
  box-shadow: inset -3px 0 8px rgba(150, 108, 84, 0.2);

  /* 字体风格 */
  font-family: 'Noto Serif SC', 'LiSu', serif;
  color: #5b3a33;
}


.sidebar.ancient-style.collapsed {
  width: 50px;
}
/*
.sidebar-toggle {
  padding: 12px;
  background-color: #ead7ce;
  color: #5b3a33;
  text-align: center;
  cursor: pointer;
  font-weight: bold;
  border-bottom: 1px solid #b07a6a;
}*/

.sidebar-content {
  display: flex;
  flex-direction: column;
  height: 100%;
  padding: 12px;
  overflow: hidden;
}
/*左侧开关按钮*/
.sider-toggle-button {
  position: absolute;
  top: 50%;
  transform: translateY(-50%);
  width: 40px;
  height: 40px;
  background: #f9f4ec;
  border: 0.5px solid #b07a6a;
  border-radius: 50%;
  box-shadow: 0 0 6px rgba(176, 122, 106, 0.5);
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 10;
  transition: left 0.35s cubic-bezier(0.4, 0, 0.2, 1);
}

.sider-toggle-button.from-aside {
  left: 220px;
  transform: translate(-50%, -50%);
}

.sider-toggle-button.from-left {
  left: 20px;
  transform: translate(-50%, -50%);
}

.sider-toggle-button:hover {
  background-color: #ead7ce;
}

.catalog-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.catalog-item.level-1{
  border-radius: 6px;
  transition: background-color 0.25s ease, box-shadow 0.25s ease;
  margin-bottom: 10px;
}

.item-header {
  display: flex;
  align-items: center;
  cursor: pointer;
  padding: 8px 14px;
  border-radius: 6px;
  background-color: transparent;
  transition: background-color 0.25s ease, box-shadow 0.25s ease;
  user-select: none;
}

.item-header:hover {
  background: linear-gradient(90deg, #f3e9e4, #f9f5f2);
  box-shadow: inset 0 0 8px rgba(91, 58, 51, 0.15);
}

.item-label {
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  font-size: 16px;
  font-weight: 600;
  margin-left: 8px;
}
/*
.feedback {
  margin-top: auto;
  padding-top: 10px;
  height: 40px;
  line-height: 40px;
  font-size: 18px;
  text-align: center;
  cursor: pointer;
  font-weight: bold;
  transition: background-color 0.3s;
  border-top: 1px solid #d8b8a8;
  color: #5b3a33;
}

.feedback:hover {
  background-color: #ead7ce;
}*/
/* 背景容器，包括渐变和图片 */
.background-container {
  display: flex;
  flex-direction: column;
  height: 100%;
  flex: 1;
  background: linear-gradient(to top, rgba(255, 255, 255, 0.7), rgba(255, 255, 255, 0.7)),
  url('../../public/assets/img/modelBackground1.JPG') no-repeat center center fixed;
  background-size: cover;
  animation: backgroundFadeIn 2s ease-out;
}


/* 聊天消息显示区 */
.chat-messages {
  flex: 1;
  padding: 20px;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 80px; /* 避免遮盖 */
  animation: messageFadeIn 1s ease-out;

  /* Firefox 滚动条样式 */
  scrollbar-width: thin;
  scrollbar-color: #aaa transparent;
}

/* WebKit 浏览器滚动条美化 */
.chat-messages::-webkit-scrollbar {
  width: 8px;
}

.chat-messages::-webkit-scrollbar-track {
  background: transparent;
}

.chat-messages::-webkit-scrollbar-thumb {
  background-color: #aaa;
  border-radius: 4px;
  transition: background-color 0.3s ease;
}

.chat-messages::-webkit-scrollbar-thumb:hover {
  background-color: #666;
}




/* 单条消息样式 */
.message {
  display: flex;
  align-items: flex-start;
  width: 100%;
  animation: fadeIn 0.5s ease-in-out;
  /*flex-direction: column;  让内容按列排列，评分栏+头像在上，消息在下 */
  gap: 10px; /* 评分栏和消息内容之间的间距 */
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 头像容器样式 */
.avatar-container {
  display: flex;
  align-items: center;
  flex-shrink: 0;
  width: 40px;
}

.bot-m {
  display: flex;
  flex-direction: row; /* 横向排列多个模型回答 */
  justify-content: space-between; /* 平分空间 */
  width: 100%;
  gap: 10px;
  margin-bottom: 10px;
}

/* 用户消息样式 */
.message.user {
  justify-content: flex-end;
  text-align: right; /* 确保文字从右边开始 */
}

.user .avatar-container {
  order: 2;
  margin-left: 10px; /* 头像与消息之间的间隔 */
}

/* 用户消息：让头像在右，消息在左 */
.user-message-container {
  display: flex;
  align-items: center;
  justify-content: flex-end; /* 右对齐 */
  gap: 10px;
  float: right;
  margin-left: auto;
}

.user-message {
  background-color: #f1f0f0; /* 修改为适合的背景颜色 */
  color: #000;
  padding: 10px 15px;
  border-radius: 20px;
  max-width: 70%;
  font-size: 16px;
  position: relative;
  margin-left: 10px; /* 向左移动10px，避免和头像太近 */
  white-space: pre-wrap; /* 保留换行符 */
  display: flex;
  flex-direction: column; /* 让内容和时间戳垂直排列 */
  text-align: left;
}

.dark-mode .user .user-message {
  background-color: #3D0000;
}

/* 机器人消息样式 */
.bot {
  justify-content: flex-start;
}

.bot .avatar-container {
  order: 0;
}

.bot-message {
  color: #000;
  padding: 10px 15px;
  border-radius: 20px;
  max-width: 90%;
  font-size: 16px;
  position: relative;
  margin-right: 10px;
  white-space: pre-wrap; /* 保留换行符 */

}

.dark-mode .bot .bot-message {
  background-color: #333;
  color: #fff;
}

/* 头像样式 */
.avatar {
  width: 40px;
  height: 40px;
  border-radius: 50%;
  object-fit: cover;
}

/* 消息文本样式 */
.message-text {
  max-width: 100%;
  word-wrap: break-word;
}

/* 消息显示图片 */
.gua-image {
  max-width: 100px;
  height: auto;
  margin-top: 10px;
  border-radius: 8px;
  box-shadow: 0 2px 5px rgba(0, 0, 0, 0.2);
}


/* 消息时间戳样式
.timestamp {
  font-size: 12px;
  color: #aaa;
  margin-top: 5px;
  text-align: right;
  display: block;
  flex-wrap: wrap;
  clear: both
}*/

/* 机器人正在输入提示 */
.typing-indicator {
  font-style: italic;
  color: #aaa;
  padding-left: 50px;
  margin-bottom: 10px;
}

/* 输入区和按钮容器 */
.chat-input-container {
  display: flex;
  align-items: center;
  padding: 15px;
  border-top: 1px solid #ddd;
  background-color: #fff;
  animation: slideUp 0.5s ease-out;
}

/* 输入框美化 */
.input-box {
  flex: 1;
  display: flex;
  align-items: center;
  background: #D3D3D3;
  border-radius: 50px;
  padding: 12px 20px;
  border: 1px solid #ddd;
  transition: background-color 0.3s;
}

.chat-input-container input {
  border: none;
  outline: none;
  background: transparent;
  flex: 1;
  font-size: 16px;
  padding-left: 10px;
}

.chat-input-container input::placeholder {
  color: #aaa;
}


/* 发送按钮美化 */
.send-button {
  padding: 10px 20px;
  background-color: #5C0000;
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


.dark-mode .send-button {
  background-color: #3B0000;
}

.dark-mode .send-button:hover {
  background-color: #5C0000;
}

/* 夜间模式按钮 */
.toggle-darkmode {
  padding: 10px 20px;
  margin-left: 10px;
  background-color: #444;
  color: white;
  border: none;
  border-radius: 50px;
  cursor: pointer;
}

.dark-mode .toggle-darkmode {
  background-color: #777;
}

.toggle-darkmode:hover {
  background-color: #555;
  transform: scale(1.05);
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideIn {
  from {
    transform: translateX(-100%);
  }
  to {
    transform: translateX(0);
  }
}

@keyframes backgroundFadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes messageFadeIn {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

/* 新增模型选择器样式 */
.model-selector-container {
  position: relative;
  margin: 10px 20px;
  z-index: 100;
}

.model-select-button {
  padding: 8px 20px;
  background-color: #5C0000;
  color: white;
  border: none;
  border-radius: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 8px;
}

.model-select-button:hover {
  background-color: #3B0000;
  transform: translateY(-1px);
}

.dark-mode .model-select-button {
  background-color: #3B0000;
}

.dark-mode .model-select-button:hover {
  background-color: #5C0000;
}

.model-menu {
  position: absolute;
  bottom: calc(100% + 8px);
  left: 0;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
  min-width: 160px;
  overflow: hidden;
  transform: translateY(-2px);
  animation: menuSlideDown 0.2s ease-out;
}

@keyframes menuSlideDown {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(-2px);
  }
}


.model-menu-item {
  padding: 12px 16px;
  cursor: pointer;
  transition: all 0.2s ease;
  color: #333;
  font-size: 14px;
}

.model-menu-item:hover {
  background-color: rgba(240, 240, 240, 0.6); /* 变浅并加透明度 */
  color: #333;
}


.model-select-button.selected-model {
  background-color: #8B0000; /* 如果模型被选中时，按钮背景颜色加深 */
  color: white;
}

.model-menu-item.selected {
  background-color: #660000; /* 如果菜单项被选中时，背景颜色加深 */
  color: white;
}

.model-menu-item.selected:hover {
  background-color: #8B0000; /* 如果菜单项被选中时，背景颜色加深 */
  color: white;
}

.model-menu-item {
  cursor: pointer;
  padding: 10px;
  background-color: #fff;
  color: #333;
}

/*.model-menu-item:hover {
//  background-color: #ddd;
}*/

.model-menu-item.disabled {
  color: #999;
  pointer-events: none;
  cursor: not-allowed;
  opacity: 0.5
}

.rating-avatar-container {
  display: flex;
  align-items: center; /* 让头像和评分栏垂直居中对齐 */
  justify-content: flex-start; /* 让评分栏和头像在一行显示 */
  gap: 10px; /* 控制评分栏和头像之间的间距 */
}

.rating {
  display: flex;
  align-items: center; /* 让文字和星星垂直居中 */
  margin-top: 10px;

}

.rating-label {
  margin-right: 5px;
  color: #808080;
  font-size: 13px;
}

.star {
  font-size: 20px;
  cursor: pointer;
  color: #808080; /* 默认灰色 */
  transition: transform 0.2s, color 0.2s;
}

.star:hover {
  transform: scale(1.2);
  /*color: #FFD700; 鼠标悬停时变金色 */
}

.rating span {
  margin-right: 5px;
}

.stars {
  display: flex;
  align-items: center;
}

.stars-with-rating {
  display: flex;
  align-items: center;
}

.filled {
  color: #FFD700 !important; /* 确保覆盖默认颜色 */
}

.rating-score {
  font-size: 16px;
  margin-left: 5px; /* 调整星星和评分数字之间的间距 */
  font-weight: bold;
}

.model-tip {
  text-align: center;
  font-size: 24px;
  color: white; /* 白色文字 */
  background: linear-gradient(135deg, #3D0000, #6C0000); /* 深红色渐变背景 */
  padding: 16px 22px;
  border-radius: 12px;
  margin: 20px auto; /* 居中 */
  width: fit-content;
  max-width: 85%;
  font-family: 'LiSu', sans-serif; /* 隶书风格 */
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.2); /* 柔和阴影 */
  animation: fadeInTip 1s ease-out, slideUp 0.8s ease-out, textAppear 1.2s ease-out; /* 添加渐变和滑动动画 + 文字出现动画 */
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 12px;
  transform: translateY(20px);
  line-height: 1.6;
}

.model-tip i {
  font-size: 24px;
  margin-right: 10px; /* 让图标和文字有些间距 */
}

@keyframes fadeInTip {
  0% {
    opacity: 0;
    transform: translateY(-20px);
  }
  100% {
    opacity: 1;
    transform: translateY(0);
  }
}

@keyframes slideUp {
  0% {
    transform: translateY(20px);
  }
  100% {
    transform: translateY(0);
  }
}

@keyframes textAppear {
  0% {
    opacity: 0;
  }
  100% {
    opacity: 1;
  }
}

/* 夜间模式适配 */
.dark-mode .model-tip {
  background: linear-gradient(135deg, #6c0000, #3D0000); /* 深红色渐变背景 */
}

/* 当选择多个模型时，应用横向布局 */
.chat-messages.horizontal-layout {
  display: flex;
  flex-direction: row; /* 设置为横向排列 */
  flex-wrap: wrap; /* 允许换行 */
  gap: 20px; /* 设置模型回答之间的间距 */
  justify-content: space-between; /* 平均分配空间 */
}

/* 机器人消息的样式，确保每个回答在横向布局下占据一定宽度 */
.message.bot {
  display: flex;
  flex-direction: column;
  width: 100%; /* 默认情况下占满一行 */
  align-items: center;
  margin-bottom: 20px; /* 每个模型之间的垂直间距 */
}

.chat-messages .message.one-model {
  width: 90%;
  margin: 0 auto; /* 居中 */
  /*text-align: center; /* 可选：文字也居中 */
}


.chat-messages .message.two-models {
  width: 48%;
}

.chat-messages .message.three-models {
  width: 30%;
}


/* 头像容器样式 */
.avatar-container {
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  width: 40px;
  margin-right: 10px;
}

/* 机器人消息内容 */
.message.bot .bot-message {
  background-color: #f1f0f0;
  padding: 10px;
  border-radius: 10px;
  max-width: 100%;
  text-align: left;
  white-space: pre-wrap; /* 保留换行符 */
}
.bot-message-container {
  position: relative;
  width: 100%;
  margin-bottom: 20px;
}
/* 评分栏样式 */
.rating {
  margin-top: 10px;
  text-align: center;
}

/* 头像和评分栏垂直排列 */
.message.bot .rating-avatar-container {
  display: flex;
  align-items: center;
  margin-bottom: 10px;
}

/* 用户消息的样式保持不变 */
.user-message-container {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-left: auto;
}

.user-message {
  background-color: #f1f0f0;
  color: #000;
  padding: 10px 15px;
  border-radius: 20px;
  max-width: 70%;
  font-size: 16px;
  position: relative;
  margin-left: 10px;
  white-space: pre-wrap;
  display: flex;
  flex-direction: column;
}


/*知识图谱相关*/
.chart-box {
  width: 100%;
  height:700px;
}

.graph-button-container {
  display: flex;
  justify-content: flex-end;
  margin:0 20px 0 ;
}

.load-btn {
  background-color: #4a90e2;
  color: white;
  padding: 8px 16px;
  font-size: 14px;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  transition: background-color 0.3s ease;
}

.load-btn:hover {
  background-color: #357ab8;
}

.load-btn:disabled,
.load-btn.is-disabled {
  background-color: #dcdfe6 !important; /* 灰色背景 */
  color: #c0c4cc !important;            /* 文字也灰一点 */
  cursor: not-allowed !important;        /* 鼠标禁止图标 */
  border-color: #ebeef5 !important;      /* 边框也淡一点 */
}
/*语音播报*/
.voice-button-container {
  margin-top: 10px;
}

.voice-button {
  background-color: #f0f0f0;
  border: 1px solid #ccc;
  border-radius: 6px;
  padding: 4px 10px;
  margin-top: 6px;
  cursor: pointer;
  font-size: 14px;
}

.voice-button:hover {
  background-color: #e8e8e8;
}

.diagram-container {
  position: relative;
  z-index: 1;
  width: 100%;
  max-width: 600px;
  min-height: 400px; /* 保持最小高度防止塌陷 */
  margin: auto;
}
</style>
