<template>
  <div class="usage-guide">
    <div class="header">
      <h1>易学探索助手 - 使用说明</h1>
      <p>探索智慧，传承经典</p>
    </div>
    <div class="footer">
      <button class="home-btn" @click="goToHome">
        <i class="home-icon">🏠</i> 返回主页
      </button>
    </div>

    <div class="guide-content">
      <div class="section">
        <h2>一、AI对话助手</h2>
        <div class="guide-steps">
          <div class="step">
            <h3>1. 选择大模型</h3>
            <p>在左侧模型列表中，最多可同时选择3个大模型进行对话。可选模型包括：</p>
            <ul>
              <li>IWEN</li>
              <li>DeepSeek</li>
              <li>Kimi</li>
            </ul>
          </div>

          <div class="step">
            <h3>2. 开始对话</h3>
            <p>在对话框中输入您的问题或想要探索的内容，点击"发送"按钮。</p>
            <p>选中的大模型将同时对您的问题进行回答。</p>
          </div>

          <div class="step">
            <h3>3. 评价回答</h3>
            <p>您可以对每个大模型的回答进行打分，点击回答旁的星星图标进行评价。</p>
          </div>

          <div class="step">
            <h3>4. 生成知识图谱</h3>
            <p>对话结束后，点击"生成知识图谱"按钮，系统将根据对话内容自动生成知识图谱。</p>
            <p>知识图谱可帮助您更好地理解和记忆对话内容。</p>
          </div>
          <div class="step">
            <h3>5. 生成变化的卦象图</h3>
            <p>当用户询问某卦象图的错卦、综卦等时，大模型可以自动检测，生成变化后的卦象图样式</p>
            <p>该样式仅用于呈现，不保存</p>
          </div>
          <div class="step">
            <h3>6. 支持历史对话</h3>
            <p>对话结束后，可快速找寻历史对话，保留您的使用数据。</p>
            <p>历史对话可帮助您更好地查找历史内容。</p>
          </div>
        </div>
      </div>

      <div class="section">
        <h2>二、智能翻译助手</h2>
        <div class="guide-steps">
          <div class="step">
            <h3>1. 准备古籍内容</h3>
            <p>您可以通过两种方式准备古籍内容：</p>
            <ul>
              <li><strong>上传文件：</strong>点击"上传古籍文件"按钮，选择本地的古籍文件（支持PNG、PDF、DOC、DOCX格式）。</li>
              <li><strong>手动输入：</strong>点击"编辑"按钮，直接在文本框中输入古籍内容。</li>
              <li>上传或输入古籍完成后，可进行繁简体切换、手动修改、清空和保存</li>
            </ul>
          </div>

          <div class="step">
            <h3>2. 句读处理</h3>
            <p>点击侧边栏"句读处理"按钮，系统将自动对古籍内容进行句读分割。</p>
            <p>界面将流式输出句读结果，输出结束后可进行繁简体切换、手动编辑、清空和保存</p>
          </div>

          <div class="step">
            <h3>3. 全文翻译</h3>
            <p>点击侧边栏"翻译"按钮，系统将对句读后的结果进行全文翻译。</p>
            <p>您可以选择以下三种翻译版本：</p>
            <ul>
              <li><strong>大众版：</strong>适合普通读者的翻译版本（默认显示）</li>
              <li><strong>白话版：</strong>通俗易懂的现代语言翻译</li>
              <li><strong>学术版：</strong>严谨的学术风格翻译</li>
            </ul>
            <p>翻译完成后，您可以点击"复制"按钮复制翻译结果，或点击"导出"按钮导出翻译内容。</p>
          </div>

          <div class="step">
            <h3>4. 查看注释</h3>
            <p>当翻译完成后，句读界面将出现“查看注释”提示，点击后将弹出句子详情窗口，包含：</p>
            <ul>
              <li>原句内容</li>
              <li>逐字注释（支持手动修改）</li>
              <li>单句翻译</li>
            </ul>
          </div>

          <div class="step">
            <h3>5. 知识图谱</h3>
            <p>翻译完成后，点击侧边栏“知识图谱”按钮将出现古籍对应的知识图谱。</p>
            <p>您可以通过以下方式操作知识图谱：</p>
            <ul>
              <li>当触碰节点和边的时候会出现相关详细解释，也可拖动节点和边</li>
              <li>点击"导出图谱"按钮将知识图谱保存为图片文件</li>
            </ul>
            <p>知识图谱可帮助您更好地理解和记忆古籍内容，构建知识体系。</p>
          </div>

          <div class="step">
            <h3>6. 生成配图</h3>
            <p>点击侧边栏“生成配图”按钮，将出现为古籍配套的图片</p>
            <ul>
              <li>出现图片后可选择导出图片</li>
              <li>当上传的古籍不合适时，将出现“该古籍不适合生成配图”的提示</li>
            </ul>
          </div>

          <div class="step">
            <h3>7.生成PPT</h3>
            <p>点击侧边栏“生成PPT”按钮，将跳转至PPT制作页面</p>
            <ul>
              <li>用户可选择上传的古籍，以及处理后的句读或翻译结果进行ppt制作，也可直接编辑文案</li>
              <li>完成文案说明后，查看生成的提纲，可进行手动编辑</li>
              <li>最后，选择模板进行完整PPT生成</li>
            </ul>
          </div>

        </div>
      </div>

      <div class="section">
        <h2>三、注意事项</h2>
        <div class="step">
          <h3>1. 文件格式</h3>
          <p>智能翻译助手支持PNG、PDF、DOC和DOCX格式的文件上传。</p>
        </div>

        <div class="step">
          <h3>2. 文本长度</h3>
          <p>为保证最佳处理效果，建议每次处理的文本长度不超过5000字。</p>
        </div>

        <div class="step">
          <h3>3. 翻译版本</h3>
          <p>不同翻译版本适用于不同场景，请根据您的需求选择合适的翻译风格。</p>
        </div>

        <div class="step">
          <h3>4. 知识图谱</h3>
          <p>知识图谱的生成基于文本内容的实体识别和关系抽取，可能需要一定时间处理。</p>
        </div>
      </div>
    </div>

    <div class="footer">
      <p>© 2025 易学探索助手 | 版本 1.0.0</p>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import router from "@/router/index.js";
const goToHome = () => {
  router.push('/')
}


</script>

<style scoped>
.usage-guide {
  font-family: 'SimSun', 'STSong', serif;
  max-width: 900px;
  margin: 0 auto;
  padding: 30px;
  background-color: #f9f1f1;
}

.header {
  text-align: center;
  margin-bottom: 40px;
  padding: 20px;
  background: linear-gradient(to right, #8b0000, #a52a2a);
  color: white;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(139, 0, 0, 0.2);
}

h1 {
  margin: 0;
  font-size: 32px;
  text-shadow: 1px 1px 3px rgba(0, 0, 0, 0.3);
}

h2 {
  color: #8b0000;
  margin-top: 30px;
  padding-bottom: 10px;
  border-bottom: 2px solid #e8c4c4;
}

h3 {
  color: #a52a2a;
  margin-top: 20px;
}

p {
  line-height: 1.6;
  margin-bottom: 10px;
}

ul {
  padding-left: 20px;
  margin: 10px 0;
}

li {
  margin-bottom: 5px;
}

.section {
  margin-bottom: 40px;
  background-color: white;
  padding: 25px;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(139, 0, 0, 0.1);
}

.guide-steps {
  counter-reset: step-counter;
}

.step {
  margin-bottom: 30px;
  position: relative;
  padding-left: 40px;
}

.step::before {
  content: counter(step-counter);
  counter-increment: step-counter;
  position: absolute;
  left: 0;
  top: 0;
  width: 30px;
  height: 30px;
  background-color: #8b0000;
  color: white;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: bold;
}

.footer {
  text-align: center;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #e8c4c4;
}

.generate-btn {
  margin-top: 15px;
  padding: 10px 20px;
  background: linear-gradient(to right, #8b0000, #a52a2a);
  color: white;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 16px;
  transition: all 0.3s;
}

.generate-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 8px rgba(139, 0, 0, 0.3);
}
</style>