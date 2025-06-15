<template>
  <div class="history-list">
    <ul>
      <li
          v-for="conv in conversations"
          :key="conv.id"
          @click="select(conv)"
          :class="['catalog-item level-2', { selected: conv.id === props.selectedId }]"
          @contextmenu.prevent="showMenu($event, conv)"
          @mouseleave="hideMenu"
          ref="itemRefs[conv.id]"
      >
        <div class="item-content">
          {{ conv.userMessage.slice(0, 10) }}...
          <span v-if="conv.pinned" class="pin-label">📌</span>
          <div class="actions" @click.stop="showMenu($event, conv)">
            <div class="ellipsis">⋮</div>
            <div
                v-show="activeMenuId === conv.id"
                class="menu"
                :style="{ top: menuPosition.top + 'px', left: menuPosition.left + 'px' }"
            >
              <div class="menu-item" @click="pinConversation(conv)"><i class="icon-pin"></i> {{ conv.pinned ? '取消置顶' : '置顶' }}</div>
              <div class="menu-item" @click="renameConversation(conv)"><i class="icon-edit"></i> 重命名</div>
              <div class="menu-item" @click="deleteConversation(conv.id)"><i class="icon-delete"></i> 删除</div>
            </div>
          </div>
        </div>
      </li>
    </ul>
    <!-- 重命名对话框 -->
    <div v-show="isRenameDialogVisible" class="dialog-overlay">
      <div class="dialog">
        <h3>重命名对话</h3>
        <input
            v-model="newName"
            type="text"
            placeholder="请输入新的对话名称"
            class="dialog-input"
        >
        <div class="dialog-actions">
          <button class="dialog-btn cancel" @click="closeRenameDialog">取消</button>
          <button class="dialog-btn confirm" @click="confirmRename">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { defineEmits, onMounted, ref, onUnmounted, nextTick, reactive } from 'vue'
import axios from "axios";
import request from '@/utils/request'
import eventBus from '@/utils/eventBus'

const emit = defineEmits(['select'])
let layoutClass = ref()
const conversations = ref([])
const props = defineProps({
  selectedId: String
})

const userId = localStorage.getItem('userId')
const itemRefs = ref({})
const activeMenuId = ref(null)
const menuPosition = ref({ top: 0, left: 0 })
const isRenameDialogVisible = ref(false)
const newName = ref('')
const currentConversation = ref(null)

// 使用更可靠的变量名
const renamingConversationId = ref(null)
// 排序函数，置顶的对话排在前面
const sortConversations = () => {
  conversations.value.sort((a, b) => {
    // 先比较是否置顶，置顶的排在前面
    if (a.pinned !== b.pinned) {
      return b.pinned ? 1 : -1;
    }
    return b.id - a.id
  })
}

const select = async (conv) => {
  localStorage.setItem('convID', conv.id)
  console.log("selectedID", conv.id)
  try {
    const res = await axios.get(`api/chatqa/getByConversation/${conv.id}`)
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

// 显示操作菜单
const showMenu = (event, conv) => {
  event.stopPropagation()
  activeMenuId.value = conv.id
  const { clientX, clientY } = event
  menuPosition.value = { top: clientY, left: clientX }
}

// 隐藏操作菜单
const hideMenu = () => {
  activeMenuId.value = null
}

// 置顶/取消置顶对话
const pinConversation = async (conv) => {
  try {
    const isPinned = conv.pinned || false
    await request.post(`/conversation/pin/${conv.id}`, { pinned: !isPinned })
    const targetIndex = conversations.value.findIndex(item => item.id === conv.id)
    if (targetIndex !== -1) {
      conversations.value[targetIndex].pinned = !isPinned
      sortConversations()
    }
    hideMenu()
  } catch (err) {
    console.error('操作失败:', err)
  }
}

// 修改重命名方法
const renameConversation = (conv) => {
  if (!conv?.id) {
    console.error('无效的对话对象', conv)
    return
  }

  // 确保存储ID
  renamingConversationId.value = conv.id
  newName.value = conv.userMessage || ''
  isRenameDialogVisible.value = true
  hideMenu()

  console.log('开始重命名对话ID:', renamingConversationId.value)
}

// 关闭重命名对话框
const closeRenameDialog = () => {
  // 先记录当前状态
  console.log('关闭对话框，当前重命名ID:', renamingConversationId.value)

  isRenameDialogVisible.value = false
  // 不立即重置ID，保留用于错误处理
  // 添加延迟重置
  setTimeout(() => {
    console.log('重置重命名ID')
    renamingConversationId.value = null
    newName.value = ''
  }, 1000) // 延迟1秒确保请求完成
}

// 确认重命名
// 修改确认重命名函数
const confirmRename = async () => {
  // 双重检查ID
  const conversationId = renamingConversationId.value
  if (!conversationId) {
    console.error('重命名失败：缺少对话ID')
    alert('无法识别当前对话，请刷新页面重试')
    return
  }

  const trimmedName = newName.value.trim()
  if (!trimmedName) {
    alert('对话名称不能为空')
    return
  }

  console.log('正在重命名对话ID:', conversationId, '新名称:', trimmedName)

  try {
    console.log('发送重命名请求到:', `/conversation/rename/${conversationId}`)

    // 修改请求格式，直接发送字符串而非JSON对象
    const response = await request.post(
        `/conversation/rename/${conversationId}`,
        trimmedName, // 直接发送字符串
        {
          headers: {
            'Content-Type': 'application/json'
          }
        }
    )

    console.log('重命名响应:', response.data)

    // 修改判断条件，检查业务状态码
    if (response.data) {
      // 更新本地数据
      const index = conversations.value.findIndex(c => c.id === conversationId)
      if (index !== -1) {
        conversations.value[index].userMessage = trimmedName
        conversations.value[index].updatedAt = Date.now()
        sortConversations()
      }

      // 通知其他组件
      eventBus.emit('conversation-renamed', {
        id: conversationId,
        newName: trimmedName
      })

      closeRenameDialog()
    }
  } catch (error) {
    console.error('重命名错误:', error)
    handleRenameError(error)
  }
}
const handleRenameError = (error) => {
  let message = '重命名失败，请稍后再试'

  // 添加详细的错误诊断
  if (error.response) {
    console.error('HTTP 状态码:', error.response.status)
    console.error('响应数据:', error.response.data)

    switch (error.response.status) {
      case 400:
        message = '请求格式错误: ' +
            (error.response.data?.message || '请检查输入')
        break
      case 401:
        message = '未授权，请重新登录'
        break
      case 404:
        message = '对话不存在或已被删除'
        break
      default:
        message = `服务器错误 (${error.response.status})`
    }
  } else if (error.request) {
    console.error('请求已发出但无响应:', error.request)
    message = '服务器无响应，请检查网络连接'
  } else {
    console.error('请求配置错误:', error.config)
    message = `请求错误: ${error.message}`
  }

  alert(message)
}

// 删除对话
const deleteConversation = async (conversationId) => {
  if (!conversationId) {
    console.error('删除失败：对话ID为空');
    return;
  }
  try {
    // 通过ID获取对话对象（避免直接使用传入的conv导致数据不一致）
    const conv = conversations.value.find(item => item.id === conversationId);
    if (!conv) {
      console.error('对话不存在');
      return;
    }
    if (confirm('确定要删除这个对话吗？此操作不可撤销。')) {
      await request.delete(`/conversation/delete/${conv.id}`)
      conversations.value = conversations.value.filter(item => item.id !== conv.id)
      hideMenu()
      // 修复：传递安全的空对象而不是 null
      if (conv.id === props.selectedId) {
        emit('select', { id: null, userMessage: "", fullMessages: [] })
      }
    }
  }
     catch (err) {
      console.error('删除失败:', err)
    }

}

const fetchConversations = async () => {
  try {
    const res = await request.get(`/conversation/getConversationsByUserId/${userId}`)
    if (res.code === '200') {
      conversations.value = res.data.map(item => ({
        id: item.id,
        userMessage: item.discribe,
        pinned: item.pinned || false,
        updatedAt: item.updatedAt || new Date().getTime()
      }))
      sortConversations()
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
.catalog-item.level-2 {
  border-radius: 6px;
  transition: background-color 0.25s ease, box-shadow 0.25s ease;
  margin-bottom: 10px;
  padding: 6px 14px;
  cursor: pointer;
  font-weight: 500;
  font-size: 14px;
  color: #7a594e;
  white-space: nowrap;
  text-overflow: ellipsis;
  user-select: none;
  position: relative;
}

.item-content {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.ellipsis {
  font-size: 16px;
  padding: 0 5px;
  color: #7a594e;
  opacity: 0;
  transition: opacity 0.2s;
}

.catalog-item.level-2:hover .ellipsis {
  opacity: 1;
}
.menu {
  position: fixed;
  background-color: #f6f0f0;
  border-radius: 8px; /* 增加圆角 */
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15); /* 增强阴影 */
  z-index: 100;
  min-width: 140px; /* 增加宽度 */
  padding: 8px 0; /* 调整内边距 */
}

.menu-item {
  padding: 10px 16px; /* 增加内边距 */
  font-size: 14px;
  color: #333;
  cursor: pointer;
  display: flex;
  align-items: center;
  transition: background-color 0.2s ease; /* 添加过渡效果 */
}

.menu-item:hover {
  background-color: #f0e6df; /* 使用更柔和的悬停色 */
  color: #5b3a33; /* 添加文字悬停色 */
  border-radius: 4px; /* 选中状态圆角 */
}

.menu-item i {
  margin-right: 8px; /* 调整图标与文字间距 */
  font-size: 16px; /* 调整图标大小 */
}

.menu-item::before {
  font-size: 16px; /* 调整图标大小 */
}



.icon-pin::before { content: '📌 '; }
.icon-edit::before { content: '✏️ '; }
.icon-delete::before { content: '🗑️ '; }

/* 悬停状态 */
.catalog-item.level-2:hover {
  background-color: #e6d4ca;
  color: #5b3a33;
}

/* 选中状态 */
.catalog-item.level-2.selected {
  background-color: #d4b7a5 !important;
  color: #7a594e !important;
  font-weight: 700;
}

/* 选中 + 悬停状态 */
.catalog-item.level-2.selected:hover {
  background-color: #caa48c !important;
}

/* 历史列表滚动条 */
.history-list {
  max-height: 700px;
  overflow-y: auto;
}

.history-list::-webkit-scrollbar {
  width: 8px;
}
.history-list::-webkit-scrollbar-track {
  background: rgba(0, 0, 0, 0.1);
  border-radius: 4px;
}
.history-list::-webkit-scrollbar-thumb {
  background-color: rgba(91, 58, 51, 0.4);
  border-radius: 4px;
}
.history-list::-webkit-scrollbar-thumb:hover {
  background-color: rgba(91, 58, 51, 0.7);
}

/* 对话框样式 */
.dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background-color: rgba(0, 0, 0, 0.3);
  display: flex;
  justify-content: center;
  align-items: center;
  z-index: 200;
}

.dialog {
  background-color: #f6f4f1;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.2);
  padding: 20px;
  width: 300px;
}

.dialog h3 {
  margin-top: 0;
  margin-bottom: 15px;
  color: #333;
}

.dialog-input {
  width: 100%;
  padding: 8px;
  margin-bottom: 15px;
  border: 1px solid #ddd;
  border-radius: 4px;
  box-sizing: border-box;
}

.dialog-actions {
  display: flex;
  justify-content: flex-end;
}

.dialog-btn {
  padding: 8px 15px;
  margin-left: 10px;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 14px;
}

.cancel {
  background-color: #f5f5f5;
  color: #333;
}

.confirm {
  background-color: #7a594e;
  color: white;
}
.pin-label {
  margin-right: 8px;
  color: #f9a825; /* 黄色图标 */
  font-size: 12px;
}
</style>