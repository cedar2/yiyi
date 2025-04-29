<template>
  <div class="itan-landing">

    <div class="background-pattern"></div>
    <div class="container">
      <div class="left-section">
        <h1>欢迎使用 ITAN 易学探索助手</h1>
        <p class="tagline">Welcome!</p>
        <div class="illustration">
          <img src="@/assets/img/yj.png"  alt="易学探索助手" />
        </div>
      </div>
      <div class="right-section">
        <div class="button-group">
          <router-link to="/outerModel" class="action-button" data-tooltip="智能问答助手，解答古籍疑难问题">AI对话助手</router-link>
          <router-link to="/upload" class="action-button"data-tooltip="古籍原文对照学习，智能断句注释">古籍辅助学习</router-link>
        </div>
      </div>
    </div>
  </div>

  <!-- 功能介绍 -->
  <div class="features">
    <h2>功能特点</h2>
    <div class="feature-grid">
      <div class="feature-card">
        <div class="feature-icon">🔍</div>
        <h3>智能问答</h3>
        <p>快速准确地找到你需要的信息</p>
      </div>

      <div class="feature-card">
        <div class="feature-icon">📚</div>
        <h3>阅读辅助</h3>
        <p>提升你的阅读体验</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">🧮</div>
        <h3>AI助手</h3>
        <p>帮助你解决各种问题</p>
      </div>
      <div class="feature-card">
        <div class="feature-icon">🌐</div>
        <h3>翻译助手</h3>
        <p>轻松翻译多种版本</p>
      </div>
    </div>
  </div>
  <!-- 页脚 -->
  <footer class="footer">
    <p>&copy; 2025 ITAN. All rights reserved.</p>
  </footer>
</template>

<script>
export default {
  name: 'ITANLanding',
};
</script>

<style scoped>


/* 顶部导航栏 */
.header {

  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 0;
  border-bottom: 1px solid #e0e0e0;
}

.logo img {
  height: 40px;
}

.nav ul {
  display: flex;
  list-style: none;
}

.nav ul li {
  margin-left: 20px;
}

.nav ul li a {
  text-decoration: none;
  color: #333;
  font-weight: 500;
}

.itan-landing {
  position: relative;
  height: 100vh;
  overflow: hidden;
}

.background-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 90%;
  background-color: #4e1717;/* 深红色背景 */
  opacity: 0.9;
  z-index: -1;
}

.container {
  display: flex;
  height: 90%;
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.left-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  color: white;
  text-align: center;
  padding: 20px;
}

.left-section h1 {
  font-size: 2.0rem;
  margin-bottom:5px;
}

.tagline {
  font-size: 1.5rem;
  margin-bottom: 30px;
}

.illustration img {
  max-width: 90%;
  height: auto;
}

.right-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
  position: relative; /* 为提示框定位提供参照 */
}

.button-group {
  display: flex;
  flex-direction: column;
  gap: 45px;
  width: 100%;
  max-width: 300px;
}

.action-button {
  padding: 12px 20px;
  background-color: #4e1717;
  color: white;
  border: none;
  border-radius: 4px;
  cursor: pointer;
  font-size: 1rem;
  text-align: center;
  text-decoration: none;
  transition: background-color 0.3s;
  position: relative; /* 为提示框定位基础 */
}

.action-button:hover {
  background-color: #6B0000;
}

/* 悬浮提示框样式 */
.action-button:hover::after {
  content: attr(data-tooltip);
  position: absolute;
  bottom: calc(100% + 10px);
  left: 50%;
  transform: translateX(-50%);
  background: rgba(78, 23, 23, 0.95);
  color: #fff;
  padding: 8px 12px;
  border-radius: 4px;
  font-size: 0.9rem;
  white-space: nowrap;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
  opacity: 0;
  animation: fadeIn 0.3s ease forwards;
  z-index: 100;
  pointer-events: none;
}

/* 提示框小三角 */
.action-button:hover::before {
  content: '';
  position: absolute;
  bottom: calc(100% + 2px);
  left: 50%;
  transform: translateX(-50%);
  border: 6px solid transparent;
  border-top-color: rgba(78, 23, 23, 0.95);
  opacity: 0;
  animation: fadeIn 0.3s ease forwards;
  z-index: 101;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translate(-50%, 5px); }
  to { opacity: 1; transform: translate(-50%, 0); }
}



/* 功能介绍 */
.features {
  margin: 50px 0;
}

.features h2 {
  text-align: center;
  margin-bottom: 30px;
  font-size: 2rem;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
}

.feature-card {
  background-color: rgba(255, 255, 255, 0.1);
  border-radius: 8px;
  padding: 20px;
  box-shadow: 0 2px 10px rgba(0, 0, 0, 0.1);
  text-align: center;
}

.feature-icon {
  font-size: 2.5rem;
  margin-bottom: 15px;
}

.feature-card h3 {
  margin-bottom: 10px;
  font-size: 1.2rem;
}

/* 页脚 */
.footer {
  text-align: center;
  padding: 20px 0;
  margin-top: 50px;
  border-top: 1px solid #e0e0e0;
  color: #666;
}
</style>