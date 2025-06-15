<template>
  <div class="common-layout">
    <el-container class="full-screen">
      <el-header class="header">
        <div class="header-left">古籍处理系统</div>
        <button class="send-button" @click="goBackHome">返回首页</button>
      </el-header>
      <el-container class="body-container">
        <!-- 侧边栏 -->
        <transition name="aside-slide">
          <el-aside v-if="showAside" class="aside">
            <div class="reader-catalog-wrapper">
              <div class="reader-catalog">
                <div class="catalog-item level-1">
                  <div class="item-header" @click="handleClick('原文')">
                    <div class="item-label">原文</div>
                  </div>
                </div>
                <div class="catalog-item level-1" :class="{ 'disabled': !originalText?.trim() }">
                  <div class="item-header" @click="handleClick('句读处理')">
                    <div class="item-label">句读处理</div>
                  </div>
                </div>
                <div class="catalog-item level-1"
                     :class="{ 'disabled': !originalText?.trim() || !segmentedText?.trim() }">
                  <div class="item-header" @click="toggleTranslate"
                       :style="{ cursor: (!originalText?.trim() || !segmentedText?.trim()) ? 'not-allowed' : 'pointer' }">
                    <div class="item-label">翻译</div>
                    <svg class="expand-icon" :class="{ expanded: showTranslateChildren }" viewBox="0 0 17 17">
                      <path d="M7.02 4.046l4 4-4 4z" fill="currentColor" />
                    </svg>
                  </div>
                  <div class="catalog-subitems" v-show="showTranslateChildren">
                    <div class="catalog-item level-2"
                         :class="{ 'disabled': !originalText?.trim() || !segmentedText?.trim() }"
                         @click="handleClick('白话版翻译')">
                      白话版翻译
                    </div>
                    <div class="catalog-item level-2"
                         :class="{ 'disabled': !originalText?.trim() || !segmentedText?.trim() }"
                         @click="handleClick('大众版翻译')">
                      大众版翻译
                    </div>
                    <div class="catalog-item level-2"
                         :class="{ 'disabled': !originalText?.trim() || !segmentedText?.trim() }"
                         @click="handleClick('学术版翻译')">
                      学术版翻译
                    </div>
                  </div>
                </div>

                <div class="catalog-item level-1"
                     :class="{ 'disabled': !originalText?.trim() || !translationResult?.trim() }">
                  <div class="item-header" @click="handleClick('知识图谱')">
                    <div class="item-label">知识图谱</div>
                  </div>
                </div>
                <div class="catalog-item level-1"
                     :class="{ 'disabled': !originalText?.trim() || !segmentedText?.trim() }">
                  <div class="item-header" @click="handleClick('生成配图')">
                    <div class="item-label">生成配图</div>
                  </div>
                </div>
                <div class="catalog-item level-1" :class="{ 'disabled': !originalText?.trim() }">
                  <div class="item-header" @click="handleClick('生成PPT')">
                    <div class="item-label">生成PPT</div>
                  </div>
                </div>
              </div>
            </div>
          </el-aside>
        </transition>
        <!-- 折叠按钮 -->
        <div class="sider-toggle-button" :class="showAside ? 'from-aside' : 'from-left'" @click="toggleAside">
          <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" fill="none" viewBox="0 0 25 25" class="icon">
            <path fill="currentColor" fill-rule="evenodd" :d="showAside ? arrowLeft : arrowRight" clip-rule="evenodd" />
          </svg>
        </div>

        <!-- 主内容区域 -->
        <el-main class="main">
          <!-- 分栏布局容器 -->
          <div class="dynamic-content-container" :class="{ 'split-view': isSplitView }">
            <!-- 初始居中原文面板 -->
            <div v-if="!isSplitView" class="content-panel center-panel">
              <v-container class="original-text-container" fluid fill-height justify="center">
                <v-card class="original-text-card elevation-3" style="width: 100%;">
                  <v-card-title class="original-text-title d-flex justify-space-between align-center">
                    <span>原文内容</span>

                    <div class="title-actions d-flex align-center">
                      <v-btn @click="toggleOriginalCharType" small text color="#7a594e" class="switch-btn mr-2">
                        {{ charType === 'simplified' ? '简' : '繁' }}
                      </v-btn>
                      <v-btn @click="triggerFileInput" small text color="#7a594e" class="upload-btn">
                        上传文件
                      </v-btn>
                    </div>
                  </v-card-title>


                  <v-card-text class="original-text-body">
                    <v-textarea v-model="originalText" placeholder="点击编辑输入内容/上传加载文献内容..." :readonly="!isEditing"
                                auto-grow variant="outlined" hide-details class="original-textarea" />
                  </v-card-text>

                  <v-card-actions class="original-text-actions">
                    <v-btn @click="clearSource = 'original'; confirmClear = true" color="#b07a6a" text
                           :disabled="!originalText?.trim()">
                      清空
                    </v-btn>
                    <v-btn v-if="!isEditing" @click="toggleEdit" color="#a67364" text>
                      编辑
                    </v-btn>
                    <v-btn v-if="isEditing" @click="handleSave" color="#7a594e" text>
                      保存
                    </v-btn>
                  </v-card-actions>
                </v-card>
                <input ref="fileInput" type="file" hidden @change="handleFileUpload"
                       accept=".pdf,.doc,.docx,.txt,.jpg,.jpeg,.png" />
              </v-container>
            </div>
            <!-- 分栏布局：左 = 句读处理，右 = 翻译/配图/原文切换 -->
            <template v-if="isSplitView">
              <!-- 左侧句读内容固定 -->
              <div class="content-panel left-panel">
                <!-- 句读处理 -->
                <v-container class="segmented-text-container" fluid fill-height justify="center">
                  <v-card class="segmented-text-card elevation-3" style="width: 100%;">
                    <v-card-title class="segmented-text-title d-flex justify-space-between align-center">
                      <span>句读结果</span>
                      <div class="title-actions d-flex align-center">
                        <v-btn @click="toggleSegmentedCharType" small text color="#7a594e" class="switch-btn">
                          {{ segmentedCharType === 'simplified' ? '简' : '繁' }}
                        </v-btn>
                      </div>
                    </v-card-title>


                    <v-card-text class="segmented-text-body">
                      <div class="segmented-textarea">
                        <div v-if="isparsingLoading" class="loading-container">
                          <div class="spinner">
                            <i class="fas fa-circle-notch fa-spin"></i>
                          </div>
                          <div class="loading-text">加载句读中，请稍候…</div>
                        </div>
                        <v-textarea v-else v-model="segmentedText" placeholder="句读结果将显示在这里"
                                    :readonly="!isEditingSegmented" auto-grow variant="outlined" hide-details
                                    class="segmented-textarea" @click="handleSegmentedTextClick"></v-textarea>
                        <div v-if="showHint" class="annotation-hint text-caption text-medium-emphasis mt-2">
                          <i class="fas fa-hand-pointer me-1" />点击文本句子可显示注释
                        </div>
                      </div>
                    </v-card-text>

                    <v-card-actions class="segmented-text-actions">
                      <v-btn @click="clearSource = 'segmented'; confirmClear = true" color="#b07a6a" text
                             :disabled="!segmentedText?.trim()">
                        清空
                      </v-btn>
                      <v-btn v-if="!isEditingSegmented" @click="toggleSegmentedEdit" color="#a67364" text>
                        编辑
                      </v-btn>
                      <v-btn v-if="isEditingSegmented" @click="handleSaveSegmentedText" color="#7a594e" text>
                        保存
                      </v-btn>

                    </v-card-actions>
                  </v-card>
                </v-container>
                <!--句读仅修改标点弹窗 -->
                <el-dialog v-model="showWarningDialog" title="警告" width="30%">
                  <span>仅可修改标点，不可修改文字</span>
                  <template #footer>
                    <el-button @click="showWarningDialog = false">确定</el-button>
                  </template>
                </el-dialog>
              </div>
              <div class="content-panel right-panel">
                <transition name="tab-flip" mode="out-in">
                  <div :key="activeTab" class="tab-content">
                    <!-- 原文 -->
                    <v-container v-if="activeTab === '原文'" class="original-text-container" fluid fill-height
                                 justify="center">
                      <v-card class="original-text-card elevation-3" style="width: 100%;">
                        <v-card-title class="original-text-title d-flex justify-space-between align-center">
                          <span>原文内容</span>

                          <div class="title-actions d-flex align-center">
                            <v-btn @click="toggleOriginalCharType" small text color="#7a594e" class="switch-btn mr-2">
                              {{ charType === 'simplified' ? '简' : '繁' }}
                            </v-btn>
                            <v-btn @click="triggerFileInput" small text color="#7a594e" class="upload-btn">
                              上传文件
                            </v-btn>
                          </div>
                        </v-card-title>


                        <v-card-text class="original-text-body">
                          <v-textarea v-model="originalText" placeholder="点击编辑输入内容/上传加载文献内容..." :readonly="!isEditing"
                                      auto-grow variant="outlined" hide-details class="original-textarea" />
                        </v-card-text>

                        <v-card-actions class="original-text-actions">
                          <v-btn @click="clearSource = 'original'; confirmClear = true" color="#b07a6a" text
                                 :disabled="!originalText?.trim()">
                            清空
                          </v-btn>
                          <v-btn v-if="!isEditing" @click="toggleEdit" color="#a67364" text>
                            编辑
                          </v-btn>
                          <v-btn v-if="isEditing" @click="handleSave" color="#7a594e" text>
                            保存
                          </v-btn>
                        </v-card-actions>
                      </v-card>
                      <input ref="fileInput" type="file" hidden @change="handleFileUpload"
                             accept=".pdf,.doc,.docx,.txt,.jpg,.jpeg,.png" />
                    </v-container>
                    <!-- 翻译 -->
                    <v-container v-if="['白话版翻译', '大众版翻译', '学术版翻译'].includes(activeTab)" class="segmented-text-container"
                                 fluid fill-height justify="center">
                      <v-card class="segmented-text-card elevation-3" style="width: 100%;">
                        <v-card-title class="segmented-text-title">
                          {{ activeTab }}结果
                          <v-spacer></v-spacer>
                        </v-card-title>

                        <v-card-text class="segmented-text-body">
                          <div class="segmented-textarea">
                            <div v-if="isLoading" class="loading-container">
                              <div class="spinner">
                                <i class="fas fa-circle-notch fa-spin"></i>
                              </div>
                              <div class="loading-text">{{ activeTab }}中，请稍候…</div>
                            </div>
                            <!-- 使用统一的文本框样式 -->
                            <div v-else class="text-content-box">
                              {{ translationResult }}
                            </div>
                          </div>
                        </v-card-text>

                        <v-card-actions class="segmented-text-actions">
                          <v-btn @click="clearSource = 'translation'; confirmClear = true" color="#b07a6a" text
                                 :disabled="!translationResult?.trim()">
                            清空
                          </v-btn>
                          <v-btn @click="copyTranslation" color="#7a594e" text>
                            复制翻译
                          </v-btn>
                          <v-btn @click="exportTranslation" color="#7a594e" text>
                            导出翻译
                          </v-btn>

                        </v-card-actions>
                      </v-card>
                    </v-container>
                    <!-- 生成配图 -->
                    <v-container v-if="activeTab === '生成配图'" class="graph-container" fluid fill-height justify="center">
                      <v-card class="graph-card elevation-3">
                        <v-card-title class="graph-title d-flex justify-space-between align-center">
                          <span>生成配图</span>
                        </v-card-title>

                        <v-card-text class="graph-body">
                          <div class="graph-display">
                            <div v-if="graphIsLoading" class="loading-container">
                              <div class="spinner">
                                <i class="fas fa-circle-notch fa-spin"></i>
                              </div>
                              <div class="loading-text">图像生成中，请稍候…</div>
                            </div>

                            <img v-else-if="graph && graph !== 'false'" :src="graph" alt="生成配图"
                                 class="generated-image" />

                            <div v-else-if="graphErrorReason" class="no-graph-tip">
                              {{ graphErrorReason }}
                            </div>

                            <div v-else class="no-graph-tip">
                              暂无图像，请先生成。
                            </div>
                          </div>
                        </v-card-text>

                        <v-card-actions class="segmented-text-actions">
                          <v-btn @click="handleExportImage" color="#b07a6a" text
                                 :disabled="!graph || graph === 'false'">
                            导出图片
                          </v-btn>
                        </v-card-actions>
                      </v-card>
                    </v-container>
                    <v-container v-if="activeTab === '知识图谱'" class="segmented-text-container" fluid fill-height
                                 justify="center">
                      <v-card class="segmented-text-card elevation-3" style="width: 100%;">
                        <v-card-title class="segmented-text-title">
                          知识图谱
                          <v-spacer></v-spacer>
                        </v-card-title>

                        <v-card-text class="segmented-text-body">
                          <div class="segmented-textarea">
                            <!-- 加载状态 -->
                            <div v-show="isHandling" class="loading-container">
                              <div class="spinner">
                                <i class="fas fa-circle-notch fa-spin"></i>
                              </div>
                              <div class="loading-text">生成知识图谱中，请稍候…</div>
                            </div>

                            <!-- 知识图谱展示容器 -->
                            <div v-show="!isHandling" class="knowledge-graph-container">
                              <div id="knowledgeGraph" ref="chartRef" class="knowledge-graph"
                                   style="width: 100%; height: 100%; min-height: 600px;"></div>
                            </div>
                          </div>
                        </v-card-text>

                        <v-card-actions class="segmented-text-actions">
                          <v-btn color="#7a594e" text @click="exportKnowledgeGraph">
                            导出图谱
                          </v-btn>
                          <v-btn color="#7a594e" text @click="openSearchModal">
                            图谱检索
                          </v-btn>
                        </v-card-actions>
                      </v-card>
                    </v-container>
                    <!-- 图谱检索模态窗口 -->
                    <v-dialog v-model="isSearchModalOpen" max-width="1200px" class="graph-search-modal">
                      <template #activator="{ on, attrs }">
                        <button v-bind="attrs" v-on="on" style="display: none;"></button>
                      </template>
                      <v-card class="search-card">
                        <v-card-title class="search-title d-flex justify-space-between align-center">
                          <span>知识图谱检索</span>
                          <v-btn @click="closeSearchModal" icon>
                            <v-icon>mdi-close</v-icon>
                          </v-btn>
                        </v-card-title>

                        <v-card-text class="search-content">
                          <!-- 检索输入框 - 使用原生input替代v-text-field -->
                          <v-container class="search-input-container">
                            <div class="custom-search-input">
                              <input
                                  type="text"
                                  v-model="searchKeyword"
                                  placeholder="请输入节点关键词"
                                  @keydown.enter="handleSearch"
                              >
                            </div>
                            <v-btn @click="handleSearch" class="search-btn">
                              搜索
                            </v-btn>
                          </v-container>

                          <!-- 检索结果图谱 -->
                          <div class="search-graph-container">
                            <div class="graph-border-wrapper">
                              <div id="searchGraph" ref="searchChartRef" class="knowledge-graph"
                                   style="width: 100%; height: 600px; min-height: 400px;"></div>
                            </div>
                          </div>
                        </v-card-text>
                      </v-card>
                    </v-dialog>

                  </div>
                </transition>
              </div>
            </template>
          </div>
          <!-- 弹窗  -->
          <!-- 清空弹窗  -->
          <v-dialog v-model="confirmClear" max-width="400">
            <v-card class="original-text-card" style="max-width: 400px;">
              <v-card-title class="original-text-title">
                确认清空内容？
              </v-card-title>
              <v-card-text class="original-text-body" style="padding: 20px;">
                此操作将清空当前文本，是否继续？
              </v-card-text>
              <v-card-actions class="original-text-actions" style="justify-content: flex-end;">
                <v-btn text @click="confirmClear = false">取消</v-btn>
                <v-btn color="#a67364" text @click="handleClear">
                  确认
                </v-btn>
              </v-card-actions>
            </v-card>
          </v-dialog>
          <!-- 保存弹窗  -->
          <v-dialog v-model="confirmSaveSuccess" max-width="400">
            <v-card class="original-text-card" style="max-width: 400px;">
              <v-card-title class="original-text-title">
                保存成功
              </v-card-title>
              <v-card-text class="original-text-body" style="padding: 20px;">
                内容已成功保存。
              </v-card-text>
              <v-card-actions class="original-text-actions" style="justify-content: flex-end;">
                <v-btn color="#a67364" text @click="confirmSaveSuccess = false">
                  确认
                </v-btn>
              </v-card-actions>
            </v-card>
          </v-dialog>
          <v-dialog v-model="showDetailDialog" max-width="800px" class="dialog-content">
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
                        <div v-for="(word, index) in getWords(selectedSentence)" :key="index" class="word-item">
                          <div class="word-text">{{ word }}</div>
                          <div class="word-meaning">
                            {{ getWordMeaning(index) }}
                          </div>
                        </div>
                      </div>
                    </div>

                    <!-- 短句释义区块 -->
                    <div class="sentence-meaning glassmorphism">
                      <h3 class="section-title">📖 全句释义</h3>
                      <p class="meaning-text">{{ getSentenceMeaning() || '深度解析生成中...' }}</p>
                    </div>
                  </div>
                </div>

              </v-card-text>

              <v-card-actions class="dialog-actions">
                <v-btn @click="showDetailDialog = false" class="action-btn text-white">
                  <v-icon left>mdi-chevron-up</v-icon>
                  <span class="btn-text">收起</span>
                </v-btn>
              </v-card-actions>
            </v-card>
          </v-dialog>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>
<script setup>
import { onBeforeUnmount, ref, watch, reactive, nextTick, watchEffect, onMounted, onUnmounted } from 'vue'
import request from "@/utils/request.js";
import { debounce } from "lodash";
import { ElNotification } from "element-plus";
import { ElMessage } from 'element-plus'
import { useRouter } from 'vue-router'

//全局变量
import { useTextStore } from '@/stores/useTextStore'
import { storeToRefs } from 'pinia'
const store = useTextStore()
const { originalText, segmentedText, translationResult, translationResults } = storeToRefs(store)

// 箭头 svg 路径
const arrowLeft =
    'M15.377 6.444a.5.5 0 0 1 0 .708l-5.293 5.292a.5.5 0 0 0 0 .707l5.293 5.293a.5.5 0 0 1 0 .707l-.354.354a.5.5 0 0 1-.707 0L8.14 13.328a.75.75 0 0 1 0-1.06l6.176-6.177a.5.5 0 0 1 .707 0z'

const arrowRight =
    'M9.623 6.444a.5.5 0 0 0 0 .708l5.293 5.292a.5.5 0 0 1 0 .707l-5.293 5.293a.5.5 0 0 0 0 .707l.354.354a.5.5 0 0 0 .707 0l6.176-6.177a.75.75 0 0 0 0-1.06L10.684 6.444a.5.5 0 0 0-.707 0z'

//返回首页
const router = useRouter()
const goBackHome = () => {
  router.push('/')
}

//侧边栏
const showAside = ref(true)
const toggleAside = () => {
  showAside.value = !showAside.value
}
const showTranslateChildren = ref(false)
const toggleTranslate = () => {
  if (!originalText.value.trim() || !segmentedText.value.trim()) {
    ElNotification({
      title: '提示',
      message: '请先完成原文输入和句读处理！',
      type: 'warning'
    });
    return;
  }
  showTranslateChildren.value = !showTranslateChildren.value;
  if (showTranslateChildren.value) {
    // 默认选中大众版翻译
    handleClick('大众版翻译');
  }
};

// 点击按钮处理
const activeTab = ref('原文')  // 默认显示"原文"
const isSplitView = ref(false) // 控制是否显示分栏布局

// 修改后的 handleClick 函数
function handleClick(tab) {
  if (tab === '原文' && !isSplitView.value) {
    activeTab.value = tab;
    return; // 初始原文居中
  }

  if (tab === '句读处理') {
    if (!originalText.value.trim()) {
      alert('请先输入或上传原文内容！');
      return;
    }
    activeTab.value = '原文'; // 右侧初始显示原文
    isSplitView.value = true; // 启动分栏布局

    if (segmentedText.value === '') {
      saveContent();
    }
    return;
  }

  if (['白话版翻译', '大众版翻译', '学术版翻译'].includes(tab)) {
    if (!originalText.value.trim() || !segmentedText.value.trim()) {
      alert('请先完成原文输入和句读处理！');
      return;
    }

    const version = tab.replace('翻译', '');
    translationVersion.value = version;
    activeTab.value = tab;
    isSplitView.value = true;

    if (translationResults.value[version]) {
      translationResult.value = translationResults.value[version];
      return;
    }

    showTranslation();
    generateKnowledgeGraph();
    return;
  }

  if (tab === '知识图谱') {
    //点进知识图谱页面
    if (!originalText.value.trim()) {
      alert('请先输入或上传原文内容！');
      return;
    }
    activeTab.value = tab;
    isSplitView.value = true;

    if (!translationResult.value?.trim()) {
      alert('请先进行翻译操作！');
      return;
    }

    generateKnowledgeGraph()

  } else {
    activeTab.value = tab;
    // 其他未指定的标签页也使用分栏布局
    isSplitView.value = true;
  }

  if (tab === '生成配图') {
    if (!originalText.value.trim() || !segmentedText.value.trim()) {
      alert('请先完成原文输入和句读处理！');
      return;
    }
    activeTab.value = tab;
    isSplitView.value = true;

    if (graph.value === '') {
      generateGraph();
    }
    return;
  }

  if (tab === '知识图谱') {
    // 待开发
    return;
  }

  if (tab === '生成PPT') {
    if (!originalText.value.trim()) {
      alert('请先完成原文输入！');
      return;
    }
    activeTab.value = tab;
    router.push('/test');
    return;
  }

  // 默认行为
  activeTab.value = tab;
  isSplitView.value = true;
}

//*****原文处理****
//原文内容
// const originalText = ref('')
// 编辑状态
const isEditing = ref(false)
//正在处理
const isparsingLoading = ref(false)
// 文件上传
const triggerFileInput = () => fileInput.value.click()
const fileInput = ref(null)

const handleFileUpload = async (event) => {
  const file = event.target.files[0]
  if (!file) return

  const formData = new FormData()
  formData.append('file', file)
  formData.append('type', file.type.split('/')[1])

  try {
    const response = await fetch('api/upload', {
      method: 'POST',
      body: formData,
    })
    const data = await response.json()
    originalText.value = data.content
    isparsingLoading.value = true
  } catch (error) {
    console.error('文件上传失败:', error)
  }
}

// 原文编辑
const toggleEdit = () => {
  isEditing.value = !isEditing.value
}

// 清空内容

// 清空变量
const confirmClear = ref(false);
const clearSource = ref(''); // 'original' 或 'segmented'
// 清空方法
const clearOriginalContent = () => {
  originalText.value = '';
  segmentedText.value = '';
  translationResult.value = '';
  translationResults.value['大众版'] = '';
  translationResults.value['白话版'] = '';
  translationResults.value['学术版'] = '';
  confirmClear.value = false;
};
const handleClear = () => {
  if (clearSource.value === 'original') {
    clearOriginalContent();
    clearGraph();
    clearAnnotation();
  } else if (clearSource.value === 'segmented') {
    clearSegmentedContent();
  } else if (clearSource.value === 'translation') {
    clearTranslationContent();
  }
};

//保存原文
const savedOriginalText = ref(''); // 用来保存上一次保存的文本，用于比对
const confirmSaveSuccess = ref(false);
const handleSave = () => {
  if (originalText.value.trim() !== savedOriginalText.value.trim()) {
    // 原文有改动，清空句读和翻译结果
    segmentedText.value = '';
    translationResult.value = '';
    for (const key in translationResults.value) {
      if (Object.hasOwnProperty.call(translationResults.value, key)) {
        translationResults.value[key] = '';
        clearAnnotation();
        clearGraph();
      }
    }
    // 更新保存的原文
    savedOriginalText.value = originalText.value.trim();
  }

  isEditing.value = false;
  confirmSaveSuccess.value = true;
};

//简体繁体切换
import * as OpenCC from 'opencc-js'

const charType = ref('simplified') // 初始设为繁体


let s2t, t2s

onMounted(() => {
  s2t = OpenCC.Converter({ from: 'cn', to: 'tw' })
  t2s = OpenCC.Converter({ from: 'tw', to: 'cn' })

  // 自动判断原文字符类型
  charType.value = detectCharType(originalText.value)
})
const detectCharType = (text) => {
  if (!text.trim()) return 'simplified'
  const converted = OpenCC.Converter({ from: 'tw', to: 'cn' })(text)
  return converted === text ? 'simplified' : 'traditional'
}


const toggleOriginalCharType = () => {
  if (!originalText.value.trim()) return

  if (charType.value === 'simplified') {
    originalText.value = s2t(originalText.value)
    charType.value = 'traditional'
  } else {
    originalText.value = t2s(originalText.value)
    charType.value = 'simplified'
  }
}

//*****句读处理****
// 句读结果
// const segmentedText = ref('')
// 句读结果编辑状态
const isEditingSegmented = ref(false)


//句读处理
const saveContent = async () => {
  isparsingLoading.value = true;
  try {
    const res = await request.post('parsing/start', {
      textData: originalText.value
    });
    const taskId = res.taskId;
    startSse(taskId);
    // 清空翻译结果
    translationResult.value = '';
    translationResults.value['大众版'] = '';
    translationResults.value['白话版'] = '';
    translationResults.value['学术版'] = '';
  } catch (error) {
    console.error('保存失败:', error);
  }
};
const startSse = (taskId) => {
  const eventSource = new EventSource(`/api/sentenceStream/${taskId}`);

  eventSource.onopen = () => {
    console.log('连接已打开');
  };

  segmentedText.value = "";
  eventSource.onmessage = (event) => {
    isparsingLoading.value = false;
    const content = event.data;
    console.log('收到内容:', content);
    const processedContent = content.replace(/<br\s*\/?>/gi, '\n');
    segmentedText.value += processedContent;
  };

  eventSource.onerror = (error) => {
    console.error('发生错误:', error);
    eventSource.close();
    isparsingLoading.value = false;
  };

  eventSource.onclose = () => {
    console.log('连接已关闭');
    isparsingLoading.value = false;
    isEditing.value = false;
  };
};

// 清空句读结果
const clearSegmentedContent = () => {
  segmentedText.value = '';
  translationResult.value = '';
  translationResults.value['大众版'] = '';
  translationResults.value['白话版'] = '';
  translationResults.value['学术版'] = '';
  confirmClear.value = false;
};

//设置为只能修改标点
// 句读结果编辑
const originalCharacters = ref('')
let lastValidSegmentedText = ''
// 弹窗控制变量
const showWarningDialog = ref(false)
// 提取非标点字符（保留文字）
const extractCharacters = (text) => {
  return text.replace(/[，。！？；：“”‘’、,.!?;:"'()\[\]【】《》\s]/g, '')
}

// 初始化 originalCharacters，当开启编辑时
const toggleSegmentedEdit = () => {
  // 只在非编辑状态下才开启编辑，避免切换回非编辑
  if (!isEditingSegmented.value && segmentedText.value) {
    isEditingSegmented.value = true
    originalCharacters.value = extractCharacters(segmentedText.value)
    lastValidSegmentedText = segmentedText.value
  }
  // 如果已经是编辑状态，点击编辑按钮不做任何操作（避免取消编辑）
}

watch(segmentedText, (newVal, oldVal) => {
  if (!isEditingSegmented.value || ignoreNextSegmentedChange.value) return
  //增加了忽视简体繁体切换
  const chars = extractCharacters(newVal)
  if (chars !== originalCharacters.value) {
    segmentedText.value = lastValidSegmentedText
    showWarningDialog.value = true
    console.log("弹窗应该出现")
  } else {
    lastValidSegmentedText = newVal
  }
})


// 保存句读结果
const savedSegmentedText = ref(''); // 保存上次保存的句读文本

const handleSaveSegmentedText = () => {
  if (segmentedText.value.trim() !== savedSegmentedText.value.trim()) {
    // 句读发生变化，清空翻译结果
    translationResult.value = '';
    for (const key in translationResults.value) {
      translationResults.value[key] = '';
    }
    // 更新已保存的句读文本
    savedSegmentedText.value = segmentedText.value.trim();
  }
  isEditingSegmented.value = false;
  confirmSaveSuccess.value = true;
};
//句读繁体简体切换
onMounted(() => {
  // 自动判断原文字符类型
  segmentedCharType.value = detectCharType(segmentedText.value)
})
const segmentedCharType = ref('simplified')
const ignoreNextSegmentedChange = ref(false)//让不能修改文字功能忽视
const toggleSegmentedCharType = () => {
  if (!segmentedText.value.trim()) return

  ignoreNextSegmentedChange.value = true  // 设置跳过

  if (segmentedCharType.value === 'simplified') {
    segmentedText.value = s2t(segmentedText.value)
    segmentedCharType.value = 'traditional'
  } else {
    segmentedText.value = t2s(segmentedText.value)
    segmentedCharType.value = 'simplified'
  }

  //使用setTimeout 清除 flag，确保 watch 执行后再重置
  setTimeout(() => {
    ignoreNextSegmentedChange.value = false
  }, 0)
}

//*****翻译******
const translationVersion = ref('大众版')
const translationVersionLabelToValue = {
  '大众版': '1',
  '白话版': '2',
  '学术版': '3'
}
// const translationResults = reactive({
//   '大众版': '',
//   '白话版': '',
//   '学术版': ''
// });

const isLoading = ref(false)
//全文的翻译结果
// const translationResult = ref('')

const showTranslation = async () => {
  console.log("开始翻译");
  const version = translationVersion.value;

  if (!segmentedText.value) {
    ElNotification({
      title: '提示',
      message: '请先选择一个句子',
      type: 'warning'
    });
    return;
  }

  // 已有缓存，直接展示
  if (translationResults.value[version]) {
    translationResult.value = translationResults.value[version];
    return;
  }

  isLoading.value = true;

  try {
    const res = await request.post('translation/translate/start', {
      text: segmentedText.value,
      translationType: translationVersionLabelToValue[version]
    });
    const taskId = res.taskId;
    translationResult.value = "";
    // 传入当前版本
    startSseTR(taskId, version);

  } catch (error) {
    console.error('保存失败:', error);
    isLoading.value = false;
  }
};

const startSseTR = (taskId, version) => {
  const eventSource = new EventSource(`/api/translation/translateStream/${taskId}`);

  eventSource.onopen = () => {
    console.log('连接已打开');
  };

  eventSource.onmessage = (event) => {
    isLoading.value = false;
    const content = event.data;
    console.log('收到内容:', content);
    const processedContent = content.replace(/<br\s*\/?>/gi, '\n');
    // 实时累加展示内容
    translationResult.value += processedContent;

    //同步更新对应版本缓存
    translationResults.value[version] += processedContent;

  };

  eventSource.onerror = (error) => {
    console.error('发生错误:', error);
    eventSource.close();
    isLoading.value = false;
  };

  eventSource.onclose = () => {
    console.log('连接已关闭');
    isLoading.value = false;
    isEditing.value = false;
  };
};

//复制翻译结果
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

//导出翻译结果
const exportTranslation = () => {
  const blob = new Blob([translationResult.value], {type: 'text/plain'})
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

//清除翻译结果
const clearTranslationContent = () => {
  const version = translationVersion.value;

  if (version === '大众版') {
    translationResults.value['大众版'] = '';
  } else if (version === '白话版') {
    translationResults.value['白话版'] = '';
  } else if (version === '学术版') {
    translationResults.value['学术版'] = '';
  }

  translationResult.value = '';
  confirmClear.value = false;
};


//******注释弹窗*****
const sentenceAnnotations = ref(new Map()) // 存储句子注释数据
const sentenceTranslations = ref(new Map())
const selectedSentence = ref('')
const showDetailDialog = ref(false)

const showHint = ref(false)

watchEffect(() => {
  showHint.value = Boolean(
      translationResult.value &&
      !isLoading.value &&
      !isparsingLoading.value
  )
})

// 句子详情弹框：处理句读文本点击事件（精确位置版本）
const handleSegmentedTextClick = (event) => {
  console.log("handleSegmentedTextClick");

  if (isEditingSegmented.value) return
  if (isLoading.value) {
    return;
  }

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
  const tempSegments = processedText.split(/([，。！？；↵])/).filter(s => s)

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
    selectedSentence.value = matched.text
        .replace(/[^\u4e00-\u9fa5]/g, '')
        .replace(/↵/g, '')

    showDetailDialog.value = true
  }
}


// 监听弹窗打开事件
watch(() => showDetailDialog.value, async (newVal) => {
  console.log(selectedSentence.value);// 可以输出
  console.log(newVal);
  if (newVal && selectedSentence.value) {
    // 检查是否已经有翻译结果，如果有则直接展示，否则调用接口
    if (!sentenceAnnotations.value.has(selectedSentence.value)) {
      await fetchSentenceAnnotations();
    }
  }
})

// 获取单句注释的方法
const fetchSentenceAnnotations = async () => {
  try {
    console.log("fetchSentenceAnnotations");
    const original = selectedSentence.value;
    const translation = translationResult.value;

    // 确保存在翻译结果
    if (!translation) {
      throw new Error('需要先获取翻译结果');
    }
    // 发送注释请求
    const response = await request.post('annotation', {
      original: original,
      translation: translation
    });

    console.log("收到", response);

    const annotationsArray = new Array(original.length).fill(null);
    response.entities?.forEach((entity, index) => {
      if (index >= 0 && index < selectedSentence.value.length) {
        annotationsArray[index] = entity;
      }
    });
    sentenceAnnotations.value.set(selectedSentence.value, annotationsArray);
    sentenceTranslations.value.set(selectedSentence.value, response.translation);
    console.log("单句翻译", response.translation)
  } catch (error) {
    console.error('注释获取失败:', error);
    ElNotification.warning('注释加载失败');
  }
};


// 辅助函数
const getWords = (sentence) => {
  // 先删除所有中文标点，再分割为字符数组
  return sentence
      // .replace(/[，。！？；：“”‘’（）《》【】]/g, '') // 删除中文标点
      .replace(/[^\u4e00-\u9fa5]/g, '')
      .split(''); // 分割为字符数组
};

// 获取单个词语解释的方法
const getWordMeaning = (index) => {
  const annotations = sentenceAnnotations.value.get(selectedSentence.value);

  // 三级判断确保数据安全
  return annotations?.[index]?.explanation ||
      (sentenceAnnotations.value.has(selectedSentence.value) ? '暂无注释' : '注释加载中...');
};

// 弹窗单句翻译方法
const getSentenceMeaning = () => {
  return sentenceTranslations.value.get(selectedSentence.value) || '翻译加载中...'
}

const clearAnnotation = () => {
  try {
    sentenceAnnotations.value.clear()
    sentenceTranslations.value.clear()
    selectedSentence.value = ''
    showDetailDialog.value = false
  } catch (error) {
    console.error('清空注释时发生错误:', error);
    ElNotification({
      title: '清空失败',
      message: error.message || '请尝试刷新页面',
      type: 'error'
    });
  }
};

//******知识图谱******
import * as echarts from 'echarts'

// 知识图谱ECharts配置
const chartRef = ref(null)
let chartInstance = null
const graphData = reactive({nodes: [], links: []});
const isHandling = ref(false);
// 新增检索相关变量
const isSearchModalOpen = ref(false);
const searchKeyword = ref('');
const searchChartInstance = ref(null);
const originalGraphData = ref({ nodes: [], links: [] }); // 保存原始图谱数据
// 创建 DOM 引用和图表实例
const searchChartRef = ref(null);
const searchChart = ref(null);


// 生成知识图谱
const generateKnowledgeGraph = async () => {
  try {
    if (chartInstance) {
      chartInstance.dispose();
    }


    // 先检查数据存在性
    const hasGraphData = graphData.nodes.length > 0;

    // 如果已有数据但图表未初始化
    if (hasGraphData) {
      console.log("使用缓存数据初始化图表");
      isHandling.value = true;

      // 添加延迟逻辑
      setTimeout(() => {
        if (chartInstance && !chartInstance.isDisposed()) {
          chartInstance.dispose();
        }
        nextTick(() => initChart(graphData));
        isHandling.value = false;
      }, 500);

      return;
    }

    // 没有缓存数据时开始加载
    isHandling.value = true;

    // 请求后端数据
    const response = await request.post('/buildKnowledgeGraph', {
      original: originalText.value,
      translation: translationResult.value
    });

    // 更新响应式数据
    graphData.nodes = [...response.graphData.nodes];
    graphData.links = [...response.graphData.links];

    // 初始化图表
    nextTick(() => {
      initChart(graphData);
    });

  } catch (error) {
    console.error('知识图谱生成失败:', error);
    isHandling.value = false;
    ElNotification({
      title: '生成失败',
      message: error.message || '请检查输入内容后重试',
      type: 'error'
    });
    // 失败时重置缓存标记
  } finally {
    isHandling.value = false;
  }
};


const nodeTypeMap = {
  1: {color: '#5470c6', symbol: 'circle', name: '典籍文献'},
  2: {color: '#91cc75', symbol: 'rect', size: 50, name: '卦象体系'},
  3: {color: '#fac858', symbol: 'circle', name: '爻位系统'},
  4: {color: '#ee6666', symbol: 'diamond', name: '哲学概念'},
  5: {color: '#73c0de', symbol: 'triangle', name: '符号系统'},
  6: {color: '#3ba272', symbol: 'roundRect', name: '思想学派'}
}

const relationTypeMap = {
  1: {color: '#999', name: '所属典籍'},
  2: {color: '#666', name: '包含爻位'},
  3: {color: '#ff4d4f', name: '哲学解释'},
  4: {color: '#69c0ff', name: '学派关联'}
}

const initChart = (data) => {
  if (chartInstance && !chartInstance.isDisposed()) {
    chartInstance.dispose();
  }

  const option = {
    tooltip: {
      formatter: ({dataType, data}) => {
        if (dataType === 'node') {
          const props = data.properties || {}
          let str = `${data.name}<br>类型：${nodeTypeMap[data.group].name}`
          if (Object.keys(props).length) str += `<br>${Object.entries(props).map(([k, v]) => `${k}: ${v}`).join(' ')}`
          return str
        }
        // return `${relationTypeMap[data.value].name}<br>${data.sourceName} → ${data.targetName}`
        return `${relationTypeMap[data.value].name}`
      }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      zoom: 0.3,
      draggable: true,
      roam: true,
      emphasis: {focus: 'adjacency'},
      force: {
        repulsion: 1500,
        edgeLength: 150
      },
      nodes: data.nodes.map(n => ({
        id: n.id,
        name: n.name,
        group: n.group,
        symbol: nodeTypeMap[n.group].symbol,
        symbolSize: nodeTypeMap[n.group].size || 35,
        itemStyle: {
          color: nodeTypeMap[n.group].color,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          position: n.group === 2 ? 'bottom' : 'right',
          formatter: ({properties}) =>
              properties?.symbol ? `${n.name}\n${properties.symbol}` : n.name
        },
        properties: n.properties || {}
      })),
      links: data.links.map(l => ({
        ...l,
        sourceName: data.nodes.find(n => n.id === l.source)?.name,
        targetName: data.nodes.find(n => n.id === l.target)?.name,
        lineStyle: {
          color: relationTypeMap[l.value].color,
          width: 1.5,
          curveness: l.value === 3 ? 0.3 : 0
        }
      }))
    }]
  }

  nextTick(() => {
    chartInstance = echarts.init(chartRef.value);

    chartInstance.setOption(option);
  });
}

// 打开检索模态窗口
const openSearchModal = () => {
  isSearchModalOpen.value = true;
  // 保存当前图谱数据到原始数据
  originalGraphData.value = {
    nodes: [...graphData.nodes],
    links: [...graphData.links]
  };
  // 在检索窗口初始化图谱
  nextTick(() => initSearchChart(originalGraphData.value));
};

// 关闭检索模态窗口
const closeSearchModal = () => {
  isSearchModalOpen.value = false;
  if (searchChartInstance.value) {
    searchChartInstance.value.dispose();
  }
};

// 初始化检索图谱（保持与原始图谱相同的布局）
const initSearchChart = (data, highlightedNodeIds = []) => {
  if (searchChartInstance.value) {
    searchChartInstance.value.dispose();
  }

  if (!searchChartRef.value) {
    console.error('搜索图表DOM元素不存在');
    return;
  }

  searchChartInstance.value = echarts.init(searchChartRef.value);

  // 窗口大小变化时重绘图表
  window.addEventListener('resize', handleResize);

  // 使用相同的配置函数确保布局一致
  const option = getCommonChartOption(data, highlightedNodeIds);
  searchChartInstance.value.setOption(option);
};

// 处理窗口大小变化
const handleResize = () => {
  if (searchChartInstance.value) {
    searchChartInstance.value.resize();
  }
};



// 处理检索
const handleSearch = () => {
  if (!searchKeyword.value.trim()) {
    alert('请输入检索关键词'); // 使用原生提示框
    return;
  }

  // 查找匹配节点
  const matchedNodes = originalGraphData.value.nodes.filter(node =>
      node.name.includes(searchKeyword.value) ||
      Object.values(node.properties || {}).some(val =>
          val && val.toString().includes(searchKeyword.value)
      )
  );

  if (matchedNodes.length === 0) {
    alert('未找到匹配的节点'); // 使用原生提示框
    return;
  }

  // 高亮所有匹配节点
  const highlightedNodes = matchedNodes.map(node => node.id);

  // 以第一个匹配节点为中心展示
  const centerNode = matchedNodes[0];

  // 更新检索图谱，高亮所有匹配节点
  initSearchChart(originalGraphData.value, highlightedNodes);

  // 等待图表渲染完成后聚焦到中心节点
  nextTick(() => {
    if (searchChartInstance.value) {
      searchChartInstance.value.dispatchAction({
        type: 'focusNode',
        seriesIndex: 0,
        dataIndex: originalGraphData.value.nodes.findIndex(node => node.id === centerNode.id)
      });
    }
  });
};

// 统一的图谱配置生成函数（修改高亮逻辑）
const getCommonChartOption = (data, highlightedNodeIds = []) => {
  return {
    tooltip: {
      formatter: ({ dataType, data }) => {
        if (dataType === 'node') {
          const props = data.properties || {};
          return `${data.name}<br>类型：${nodeTypeMap[data.group].name}<br>${Object.entries(props).map(([k, v]) => `${k}: ${v}`).join('<br>')}`;
        }
        return `${relationTypeMap[data.value].name}`;
      }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      zoom: 0.3,
      draggable: true,
      roam: true,
      emphasis: { focus: 'adjacency' },
      force: {
        repulsion: 1500,
        edgeLength: 150
      },
      nodes: data.nodes.map(node => {
        // 判断节点是否需要高亮
        const isHighlighted = highlightedNodeIds.includes(node.id);

        return {
          id: node.id,
          name: node.name,
          group: node.group,
          symbol: nodeTypeMap[node.group].symbol,
          symbolSize: nodeTypeMap[node.group].size || 35,
          itemStyle: {
            // 高亮节点使用特殊颜色
            color: isHighlighted ? '#ff4d4f' : nodeTypeMap[node.group].color,
            borderColor: '#fff',
            borderWidth: 2,
            // 添加高亮效果
            shadowBlur: isHighlighted ? 15 : 0,
            shadowColor: isHighlighted ? '#ff4d4f' : 'transparent'
          },
          label: {
            show: true,
            position: node.group === 2 ? 'bottom' : 'right',
            formatter: ({ properties }) =>
                properties?.symbol ? `${node.name}\n${properties.symbol}` : node.name
          },
          properties: node.properties || {}
        };
      }),
      links: data.links.map(l => ({
        ...l,
        sourceName: data.nodes.find(n => n.id === l.source)?.name,
        targetName: data.nodes.find(n => n.id === l.target)?.name,
        lineStyle: {
          color: relationTypeMap[l.value].color,
          width: 1.5,
          curveness: l.value === 3 ? 0.3 : 0,
          // 与节点高亮相关的边也高亮
          opacity: highlightedNodeIds.includes(l.source) || highlightedNodeIds.includes(l.target) ? 1 : 0.5
        }
      }))
    }]
  };
};



// 获取图谱配置（支持高亮节点）
const getChartOption = (data, highlightedNodeIds) => {
  return {
    tooltip: {
      formatter: ({ dataType, data }) => {
        if (dataType === 'node') {
          const props = data.properties || {};
          return `${data.name}<br>类型：${nodeTypeMap[data.group].name}<br>${Object.entries(props).map(([k, v]) => `${k}: ${v}`).join('<br>')}`;
        }
        return `${relationTypeMap[data.value].name}`;
      }
    },
    series: [{
      type: 'graph',
      layout: 'force',
      zoom: 0.3,
      draggable: true,
      roam: true,
      emphasis: { focus: 'adjacency' },
      force: {
        repulsion: 1500,
        edgeLength: 150
      },
      nodes: data.nodes.map(node => ({
        ...node,
        itemStyle: {
          ...nodeTypeMap[node.group],
          borderColor: highlightedNodeIds.includes(node.id) ? '#ff4d4f' : '#fff',
          borderWidth: highlightedNodeIds.includes(node.id) ? 4 : 2
        },
        label: {
          show: true,
          position: node.group === 2 ? 'bottom' : 'right',
          formatter: ({ properties }) =>
              properties?.symbol ? `${node.name}\n${properties.symbol}` : node.name
        }
      })),
      links: data.links.map(l => ({
        ...l,
        lineStyle: {
          ...relationTypeMap[l.value],
          opacity: highlightedNodeIds.includes(l.source) || highlightedNodeIds.includes(l.target) ? 1 : 0.5
        }
      }))
    }]
  };
};

// 监听原始图谱数据变化，同步到检索窗口
watch(() => graphData.nodes, () => {
  if (isSearchModalOpen.value) {
    originalGraphData.value.nodes = [...graphData.nodes];
    initSearchChart(originalGraphData.value);
  }
}, { deep: true });
// 监听数据变化
watchEffect(() => {
  console.log("watchEffect")
  if (graphData.nodes?.length > 0 && graphData.links?.length > 0) {
    // 确保容器已渲染
    nextTick(() => initChart(graphData));
  }
});

const clearGraph = () => {
  try {
    // 销毁图表实例
    if (chartInstance) {
      chartInstance.dispose();
      chartInstance = null;
    }

    // 清空响应式数据
    Object.assign(graphData, {
      nodes: [],
      links: []
    });

    // 重置加载状态
    isHandling.value = false;

    console.log('知识图谱已清空');
  } catch (error) {
    console.error('清空图谱时发生错误:', error);
    ElNotification({
      title: '清空失败',
      message: error.message || '请尝试刷新页面',
      type: 'error'
    });
  }
};

const exportKnowledgeGraph = () => {
  try {
    if (!chartInstance) {
      throw new Error('图表未初始化')
    }
    if (isHandling.value) {
      alert('正在生成请等候');
      return;
    }

    // 获取当前时间作为文件名
    const timestamp = new Date().toLocaleString().replace(/[/:]/g, '-')
    const fileName = `知识图谱-${timestamp}`

    // 获取图表图片数据
    const dataURL = chartInstance.getDataURL({
      type: 'png',
      pixelRatio: 2,
      backgroundColor: '#fff'
    })

    // 创建临时链接
    const link = document.createElement('a')
    link.href = dataURL
    link.download = `${fileName}.png`
    document.body.appendChild(link)
    link.click()
    document.body.removeChild(link)

  } catch (error) {
    console.error('导出失败:', error)
    ElNotification({
      title: '导出失败',
      message: error.message || '请先生成知识图谱',
      type: 'error',
      duration: 3000
    })
  }
}

//*****生成图片*****
const graph = ref('')
const graphIsLoading = ref(false)
const graphErrorReason = ref('')
const generateGraph = async () => {
  try {
    graphIsLoading.value = true
    graph.value = null
    graphErrorReason.value = '' // 清空错误提示

    const res = await request.post('/image/generate', {
      text: segmentedText.value
    })

    if (res?.imageUrl === 'false') {
      // 后端返回 false 字符串，表示不可生成图像
      graphErrorReason.value = '该古籍不适合生成配图'
    } else if (res?.imageUrl) {
      graph.value = res.imageUrl
    } else {
      ElMessage.error('生成失败，未返回图像地址')
    }
  } catch (error) {
    console.error('生成配图出错:', error)
    ElMessage.error('生成配图请求失败，请稍后重试')
  } finally {
    graphIsLoading.value = false
  }
}
// 临时用64.png测试
// const generateGraph = async () => {
//   try {
//     graphIsLoading.value = true
//     graph.value = null
//     graphErrorReason.value = '' // 清空错误提示
//
//     // 模拟延迟以测试 loading 效果
//     await new Promise(resolve => setTimeout(resolve, 1000))
//
//     // 临时使用本地图像测试样式
//     graph.value = new URL('@/assets/img/64.png', import.meta.url).href
//
//     // 如果你想测试“不适合生成”状态，可以注释上面一行并使用下面一行：
//     // graphErrorReason.value = '该古籍不适合生成配图'
//
//   } catch (error) {
//     console.error('模拟图像加载出错:', error)
//     ElMessage.error('图像加载失败')
//   } finally {
//     graphIsLoading.value = false
//   }
// }
// 导出图片
const handleExportImage = async () => {
  if (!graph.value || graph.value === 'false') return

  try {
    // 是 base64，直接导出
    if (graph.value.startsWith('data:image')) {
      const link = document.createElement('a')
      link.href = graph.value
      link.download = '生成配图.png'
      link.click()
    } else {
      // URL 直接新窗口打开
      window.open(graph.value, '_blank')
    }
  } catch (err) {
    console.error('导出图片失败:', err)
    ElMessage.error('导出图片失败，请检查图片链接或网络连接')
  }
}

</script>

<style scoped>
/* 保持原有基础样式 */
html,
body,
#app {
  height: 100%;
  margin: 0;
  padding: 0;
  box-sizing: border-box;
  overflow: hidden;
}

*,
*::before,
*::after {
  box-sizing: inherit;
}

.common-layout {
  height: 100%;
  display: flex;
  flex-direction: column;
}

.full-screen {
  display: flex;
  flex-direction: column;
  height: 100%;
}

.header {
  height: 60px;
  line-height: 60px;
  background: linear-gradient(90deg, #f3e9e4, #fff8f5);
  /* 柔和渐变与主背景一致 */
  color: #5b3a33;
  /* 深棕色文字，呼应卡片内容 */
  display: flex;
  justify-content: space-between;
  padding: 0 24px;
  align-items: center;
  font-family: 'Noto Serif SC', serif;
  font-weight: 700;
  font-size: 20px;
  box-shadow: 0 2px 8px rgba(91, 58, 51, 0.1);
  /* 温和阴影 */
  user-select: none;
  z-index: 20;
  border-bottom: 1px solid #d9cfc7;
  /* 细致边框 */
  backdrop-filter: saturate(180%) blur(4px);
  -webkit-backdrop-filter: saturate(180%) blur(4px);
}

.send-button {
  height: 36px;
  line-height: 36px;
  padding: 0 20px;
  font-size: 15px;
  font-family: 'Noto Serif SC', serif;
  font-weight: 600;
  color: #5b3a33;
  background-color: #f3e9e4;
  border: 1px solid #d8cbb5;
  border-radius: 18px;
  /* 圆润按钮 */
  box-shadow: 0 2px 5px rgba(91, 58, 51, 0.1);
  cursor: pointer;
  transition: all 0.3s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.send-button:hover {
  background-color: #e6dad3;
  box-shadow: 0 2px 8px rgba(91, 58, 51, 0.15);
}


.body-container {
  flex: 1;
  display: flex;
  overflow: hidden;
  min-height: 0;
}

.aside {
  width: 220px;
  background-color: #f9f4ec;
  border-right: 1px solid #b07a6a;
  display: flex;
  flex-direction: column;
  box-sizing: border-box;
  overflow-y: auto;
  padding: 12px 0;
  font-family: 'Noto Serif SC', serif;
  color: #5b3a33;
  user-select: none;
  transition: width 0.35s cubic-bezier(0.4, 0, 0.2, 1);
  box-shadow: inset -2px 0 6px rgba(91, 58, 51, 0.12);
}

.main {
  flex: 1;
  padding: 20px;
  overflow: auto;
  background-color: #fff8f5;
  box-sizing: border-box;
  min-height: 0;
}

.reader-catalog-wrapper {
  width: 100%;
  padding: 0 16px;
}

.reader-catalog {
  display: flex;
  flex-direction: column;
  position: relative;
}

.catalog-item.level-1 {
  margin-bottom: 10px;
  font-size: 15px;
  font-weight: 600;
  letter-spacing: 0.02em;
  border-radius: 6px;
  transition: background-color 0.25s ease, box-shadow 0.25s ease;
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

.catalog-item.disabled .item-header {
  cursor: not-allowed;
  background-color: #f3f3f3;
  color: #aaa;
}

.item-label {
  flex: 1;
  overflow: hidden;
  white-space: nowrap;
  text-overflow: ellipsis;
  color: #5b3a33;
}

.expand-icon {
  width: 18px;
  height: 18px;
  margin-right: 10px;
  fill: #a67364;
  transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1),
  fill 0.3s ease;
}

.expand-icon.expanded {
  transform: rotate(90deg);
  fill: #5b3a33;
}

.catalog-subitems {
  margin-left: 28px;
  margin-top: 6px;
  overflow: hidden;
}

.catalog-item.level-2 {
  padding: 6px 14px;
  cursor: pointer;
  border-radius: 5px;
  font-weight: 500;
  font-size: 13px;
  color: #7a594e;
  transition: background-color 0.25s ease, color 0.25s ease;
  white-space: nowrap;
  text-overflow: ellipsis;
  user-select: none;
}

.catalog-item.level-2:hover {
  background-color: #e6d4ca;
  color: #5b3a33;
}

.aside-slide-enter-from,
.aside-slide-leave-to {
  width: 0;
  opacity: 0;
  overflow: hidden;
}

.aside-slide-enter-active,
.aside-slide-leave-active {
  transition: width 0.35s cubic-bezier(0.4, 0, 0.2, 1), opacity 0.3s ease;
}

.aside-slide-enter-to,
.aside-slide-leave-from {
  width: 220px;
  opacity: 1;
}

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

/* ===== 优化后的样式 ===== */

/* 动态布局容器 */
.dynamic-content-container {
  display: flex;
  width: 100%;
  height: calc(100vh - 120px);
  /* 固定高度，确保内容区域高度一致 */
  transition: all 0.5s ease;
  perspective: 1200px;
  /* 3D视角效果 */
}

/* 分栏视图样式 */
.split-view {
  gap: 30px;
  /* 增加间距，让翻书效果有空间 */
}

/* 内容面板通用样式 - 固定高度 */
.content-panel {
  height: 100%;
  display: flex;
  flex-direction: column;
  transition: all 0.4s ease-out;
}

/* 原文和其他容器 */
.original-text-container,
.segmented-text-container {
  background-color: transparent;
  height: 100%;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 0;
}

/*按钮样式*/
.title-actions {
  gap: 12px;
  /* 让按钮之间有空隙 */
}

.switch-btn,
.upload-btn {
  font-weight: 500;
}


/* 卡片统一样式 */
.original-text-card,
.segmented-text-card {
  background-color: #f9f4ec;
  color: #5b3a33;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(91, 58, 51, 0.15);
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
  /* 确保卡片填充整个容器 */
}

/* 卡片标题 */
.original-text-title,
.segmented-text-title {
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
  font-size: 1.25rem;
  color: #7a594e;
  background-color: #f3e9e4;
  padding: 12px 20px;
  border-bottom: 1px solid #b07a6a;
  align-items: center;
  flex-shrink: 0;
  /* 防止标题被压缩 */
  z-index: 2;
  /* 确保标题在最上层 */
}

/* 内容部分 */
.original-text-body,
.segmented-text-body {
  padding: 16px 20px;
  background-color: #fff;
  border-left: 1px solid #e3d8d1;
  border-right: 1px solid #e3d8d1;
  border-bottom: 1px solid #e3d8d1;
  flex-grow: 1;
  /* 内容区域自动填充剩余空间 */
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

/* 文本域统一样式 */
.original-textarea,
.segmented-textarea,
.text-content-box {
  font-family: 'Noto Serif SC', serif;
  font-size: 1rem;
  color: #5b3a33;
  background-color: #fff;
  border-radius: 6px;
  padding: 8px;
  width: 100%;
  height: 100%;
  min-height: 300px;
  border: 1px solid #e3d8d1;
  resize: none;
  flex-grow: 1;
  /* 自动填充剩余空间 */
  overflow-y: auto;
}

/* 翻译内容显示框 - 与textarea样式相同 */
.text-content-box {
  white-space: pre-wrap;
  line-height: 1.6;
}

/* 底部操作区 */
.original-text-actions,
.segmented-text-actions {
  background-color: #f3e9e4;
  padding: 8px 20px;
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid #b07a6a;
  border-radius: 0 0 10px 10px;
  flex-shrink: 0;
  /* 防止底部被压缩 */
  z-index: 2;
  /* 确保底部在最上层 */
}

/* 滚动条样式 */
.original-text-body::-webkit-scrollbar,
.original-textarea::-webkit-scrollbar,
.segmented-text-body::-webkit-scrollbar,
.segmented-textarea::-webkit-scrollbar,
.text-content-box::-webkit-scrollbar {
  display: none;
}

.original-text-body,
.original-textarea,
.segmented-text-body,
.segmented-textarea,
.text-content-box {
  scrollbar-width: none;
  -ms-overflow-style: none;
  overflow-y: auto;
}

/* 分栏布局样式 */
.left-panel {
  width: 47%;
  transition: all 0.5s ease;
  transform-origin: left center;
  min-width: 300px;
}

/* 右侧面板 - 翻书效果 */
.right-panel {
  width: 53%;
  position: relative;
  transition: all 0.5s ease;
  transform-origin: left center;
  box-shadow: -8px 0 15px -6px rgba(91, 58, 51, 0.2);
}

/* 添加tab切换的翻书动画类 */
.tab-flip-enter-active {
  animation: flip-in 0.6s ease-out forwards;
}

/* 所有显示的面板都添加翻书动画 */
.right-panel > * {
  animation: flip-in 0.6s ease-out forwards;
}

/* 翻书效果动画 */
@keyframes flip-in {
  from {
    opacity: 0.3;
    transform: rotateY(-40deg) translateX(80px);
    box-shadow: -2px 0 5px rgba(91, 58, 51, 0.1);
  }

  to {
    opacity: 1;
    transform: rotateY(0) translateX(0);
    box-shadow: -8px 0 15px -6px rgba(91, 58, 51, 0.2);
  }
}

/* 居中面板 */
.center-panel {
  width: 100%;
  max-width: 800px;
  margin: 0 auto;
  animation: fade-in 0.5s ease;
}

@keyframes fade-in {
  from {
    opacity: 0;
    transform: translateY(20px);
  }

  to {
    opacity: 1;
    transform: translateY(0);
  }
}

/* 加载指示器样式 */
.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  width: 100%;
  color: #7a594e;
  font-family: 'Noto Serif SC', serif;
}

.spinner {
  margin-bottom: 15px;
  font-size: 1.5rem;
}

.loading-text {
  font-size: 0.95rem;
}

/* 卡片翻页阴影效果 */
.right-panel::before {
  content: '';
  position: absolute;
  top: 0;
  left: -20px;
  height: 100%;
  width: 20px;
  background: linear-gradient(to right, rgba(0, 0, 0, 0.02), rgba(0, 0, 0, 0.1));
  pointer-events: none;
  z-index: 1;
}

/* 文本内容容器 */
.segmented-textarea {
  display: flex;
  flex-direction: column;
  height: 100%;
  width: 100%;
}

/* 添加新的包装元素样式 */
.tab-content {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
}

/* 调整翻书动画，现在应用于包装元素 */
.tab-flip-enter-active {
  animation: flip-in 0.6s ease-out forwards;
}

.tab-flip-leave-active {
  animation: flip-out 0.3s ease-in forwards;
}

@keyframes flip-in {
  from {
    opacity: 0.3;
    transform: rotateY(-40deg) translateX(80px);
    box-shadow: -2px 0 5px rgba(91, 58, 51, 0.1);
  }

  to {
    opacity: 1;
    transform: rotateY(0) translateX(0);
    box-shadow: -8px 0 15px -6px rgba(91, 58, 51, 0.2);
  }
}

@keyframes flip-out {
  from {
    opacity: 1;
    transform: rotateY(0) translateX(0);
  }

  to {
    opacity: 0;
    transform: rotateY(20deg) translateX(-60px);
  }
}

/*图片样式*/
/* 容器继承 segmented 样式 */
.graph-container {
  background-color: transparent;
  height: 100%;
  width: 100%;
  display: flex;
  justify-content: center;
  align-items: flex-start;
  padding: 0;
}

/* 卡片风格继承 segmented-card */
.graph-card {
  background-color: #f9f4ec;
  color: #5b3a33;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(91, 58, 51, 0.15);
  display: flex;
  flex-direction: column;
  width: 100%;
  height: 100%;
}

/* 卡片标题样式 */
.graph-title {
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
  font-size: 1.25rem;
  color: #7a594e;
  background-color: #f3e9e4;
  padding: 12px 20px;
  border-bottom: 1px solid #b07a6a;
  align-items: center;
  flex-shrink: 0;
  z-index: 2;
}

/* 卡片内容主体 */
.graph-body {
  padding: 16px 20px;
  background-color: #fff;
  border-left: 1px solid #e3d8d1;
  border-right: 1px solid #e3d8d1;
  border-bottom: 1px solid #e3d8d1;
  flex-grow: 1;
  overflow-y: auto;
  display: flex;
  flex-direction: column;
}

/* 显示区域居中 */
.graph-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  flex-grow: 1;
  min-height: 300px;
  padding: 20px;
}

/* 图像样式 */
.generated-image {
  max-width: 100%;
  max-height: 100%;
  border-radius: 8px;
  box-shadow: 0 4px 12px rgba(91, 58, 51, 0.1);
  object-fit: contain;
}

/* 加载状态 */
.loading-container {
  color: #7a594e;
  font-family: 'Noto Serif SC', serif;
  font-size: 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.loading-container .spinner {
  font-size: 2rem;
  margin-bottom: 8px;
}

/* 无图时提示 */
.no-graph-tip {
  color: #aaa;
  font-style: italic;
  font-family: 'Noto Serif SC', serif;
  font-size: 1rem;
}

.graph-actions {
  padding: 8px 16px;
  justify-content: flex-end;
}

/* 注释弹窗 */
/* 注释弹窗 */
.detail-card {
  background: linear-gradient(135deg, #c7bfbf 0%, #522d2d 100%);
}

.background-layer {
  background: linear-gradient(135deg, #c7bfbf 0%, #522d2d 100%);
  border-radius: 8px;
  padding: 16px;
}

.sentence-detail {
  padding: 20px;
}

.sentence-header,
.word-explanations,
.sentence-meaning {
  margin-bottom: 20px;
}

.sentence-content {
  font-size: 1.3rem;
  line-height: 1.6;
  color: var(--primary-dark);
  text-align: center;
  margin: 20px 0;
  font-weight: 500;
}

.glassmorphism {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(8px);
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 16px;
  box-shadow: 0 4px 6px rgba(0, 0, 0, 0.05);
}

.section-title {
  color: var(--primary-main);
  border-left: 4px solid var(--accent-gold);
  padding-left: 12px;
  margin-bottom: 16px;
  font-size: 1.1rem;
  text-shadow: 1px 1px 2px rgba(218, 216, 216, 0.5);
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

/* 全局或当前组件内添加 */
.action-btn .btn-text {
  color: white;
}

/* 知识图谱 */
.knowledge-graph-container {
  width: 100%;
  height: 100%;
  position: relative;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.05),
  0 2px 4px -1px rgba(0, 0, 0, 0.02);
  overflow: hidden;
}

.knowledge-graph {
  width: 100%;
  height: 70vh;
  min-height: 500px;
  background: transparent !important;
  transition: all 0.3s ease;

  /* 添加内嵌边框效果 */

  &::after {
    content: '';
    position: absolute;
    top: 0;
    left: 0;
    right: 0;
    bottom: 0;
    border: 1px solid rgba(220, 220, 220, 0.4);
    pointer-events: none;
    border-radius: 8px;
  }
}
/* 新增检索界面样式 */
.graph-search-modal {
  --el-dialog-width: 1200px;
}

.search-card {
  background-color: #f9f4ec;
  border-radius: 10px;
  box-shadow: 0 4px 10px rgba(91, 58, 51, 0.15);
}

.search-title {
  font-weight: 700;
  font-family: 'Noto Serif SC', serif;
  font-size: 1.25rem;
  color: #7a594e;
  background-color: #f3e9e4;
  padding: 12px 20px;
  border-bottom: 1px solid #b07a6a;
}

.search-input-container {
  display: flex;
  gap: 12px;
  padding: 20px;
  background-color: #fff;
  border-bottom: 1px solid #e3a594;
  align-items: center;
}

/* 自定义搜索输入框样式 */
.custom-search-input {
  flex: 1;
  height: 36px;
  border: 1px solid #7a594e;
  border-radius: 4px;
  overflow: hidden;
  display: flex;
  align-items: center;
  transition: all 0.3s;
}

.custom-search-input:hover {
  border-color: #5b3a33;
}

.custom-search-input:focus-within {
  border: 2px solid #7a594e;
  box-shadow: 0 0 0 2px rgba(123, 89, 78, 0.1);
}

.custom-search-input input {
  flex: 1;
  height: 100%;
  border: none;
  outline: none;
  padding: 0 12px;
  font-size: 14px;
  color: #5b3a33;
  background-color: transparent;
}

/* 按钮样式 */
.search-btn {
  height: 36px !important;
  padding: 0 20px !important;
  border-radius: 4px !important;
  background-color: #7a594e !important;
  border: 1px solid #7a594e !important;
  color: white !important;
  transition: all 0.3s !important;
  display: flex !important;
  align-items: center !important;
  justify-content: center !important;
}

.search-btn:hover {
  background-color: #5b3a33 !important;
  border-color: #5b3a33 !important;
}
/* 图谱容器边框美化 */
.search-graph-container {
  margin: 20px;
}

.graph-border-wrapper {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  background: #fff;
}

.graph-border-wrapper::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border: 1px solid transparent;
  border-radius: 8px;
  background: #faf6f5;

  mask-composite: exclude;
  pointer-events: none;
}

.knowledge-graph {
  position: relative;
  z-index: 1;
  background-color: #f9f9f9;
}
</style>