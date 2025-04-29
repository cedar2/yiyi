<template>
  <div class="history-list">
    <ul>
      <li
          v-for="conv in conversations"
          :key="conv.id"
          @click="select(conv)"
          :class="['history-item', { selected: conv.id === selectedId }]"
      >
        {{ conv.userMessage.slice(0, 10) }}...
      </li>
    </ul>
  </div>
</template>


<script setup>
import { defineEmits, onMounted, ref ,onUnmounted} from 'vue'
import axios from "axios";
import request from '@/utils/request'
import eventBus from '@/utils/eventBus'
const emit = defineEmits(['select'])
let layoutClass = ref()
const conversations = ref([])
const selectedId = ref(null);


const userId = localStorage.getItem('userId')

const select = async (conv) => {
  selectedId.value = conv.id
  localStorage.setItem('convID',selectedId.value)
  console.log("selectedID",selectedId.value)
  try {
    //console.log(userId)
    const res = await axios.get(`api/chatqa/getByConversation/${conv.id}`)
    //console.log(res.data.data)
    if (res.data.code === '200') {
      const items = res.data.data
      const fullMessages = []

      items.forEach(item => {
        // 用户问题
        fullMessages.push({
          role: 'user',
          content: item.question
        })

        if(item.score1 === -1){
          item.score1 = null
        }
        if(item.score2 === -1){
          item.score2 = null
        }
        if(item.score3 === -1){
          item.score3 = null
        }
        layoutClass = item.modelCount === 2 ? 'two-models'
            : item.modelCount >= 3 ? 'three-models'
                : 'one-model'

        // 模型回答，只要有内容就添加
        if (item.answer1) {
          fullMessages.push({
            role: 'local',
            content: item.answer1,
            score : item.score1,
            modelCount: layoutClass
          })
        }
        if (item.answer2) {
          fullMessages.push({
            role: 'kimi',
            content: item.answer2,
            score : item.score2,
            modelCount: layoutClass
          })
        }
        if (item.answer3) {
          fullMessages.push({
            role: 'deepseek',
            content: item.answer3,
            score : item.score3,
            modelCount: layoutClass
          })
        }
      })
      console.log('fullMessage' , fullMessages)

      emit('select', {
        id: conv.id,
        userMessage: conv.userMessage,
        fullMessages
      })
    }
  } catch (err) {
    console.error('获取完整对话失败:', err)
  }
}


const fetchConversations = async () => {
  try {
    const res = await request.get(`/conversation/getConversationsByUserId/${userId}`)
    if (res.code === '200') {
      conversations.value = res.data.map(item => ({
        id: item.id,
        userMessage: item.discribe
      }))
    }
  } catch (err) {
    console.error('获取历史对话失败:', err)
  }
}

// 注册监听刷新
onMounted(() => {
  fetchConversations()
  eventBus.on('refresh-history', fetchConversations)
})
onUnmounted(() => {
  eventBus.off('refresh-history', fetchConversations)
})
</script>

<style scoped>
.history-item {
  cursor: pointer;
  padding: 10px;
  border-bottom: 1px solid #eee;
}

.history-item:hover {
  background-color: rgba(240, 240, 240, 0.4);
}

.history-item.selected {
  background-color: rgba(240, 240, 240, 0.2);
  color: white;
}

.history-item.selected:hover {
  background-color: rgba(240, 240, 240, 0.6);
}
.history-list {
  max-height: 650px; /* 或你想设定的高度，比如 100vh */
  overflow-y: auto;
}
/* Chrome, Edge, Safari 滚动条样式 */
.history-list::-webkit-scrollbar {
  width: 8px;
}

.history-list::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1); /* 滚动轨道背景 */
  border-radius: 4px;
}

.history-list::-webkit-scrollbar-thumb {
  background-color: rgba(255, 255, 255, 0.4); /* 滚动条本体颜色 */
  border-radius: 4px;
  transition: background-color 0.3s;
}

.history-list::-webkit-scrollbar-thumb:hover {
  background-color: rgba(255, 255, 255, 0.7); /* 悬停变亮 */
}
</style>