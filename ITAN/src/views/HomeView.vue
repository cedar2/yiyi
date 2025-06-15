<template>
  <div class="itan-landing-page">
    <!-- 顶部导航栏 -->
    <div class="topbar">
      <div class="topbar-content">
        <div class="logo-section">
          <img src="@/assets/img/yi-logo.png" alt="ITAN Logo" />
          <h1 class="logo-text">易学探索助手</h1>
        </div>

        <nav class="nav-links">
          <button @click="usage" class="nav-link">使用说明</button>
          <a href="https://jingdian.ancientbooks.cn/#/6" target="_blank" class="nav-link">资源库</a>
          <a href="https://yixueziliao.com/" target="_blank" class="nav-link">易学社区</a>
        </nav>

        <div class="navbar-user-info">
          <template v-if="isLoggedIn">
            <span class="nickname">{{ nickname }}</span>
            <button class="nav-link" @click="logout">退出</button>
            <button class="nav-link" @click="toUser">修改个人信息</button>
          </template>
          <template v-else>
            <button class="nav-link" @click="goLogin">登录</button>
            <button class="nav-link" @click="goRegister">注册</button>
          </template>
        </div>
      </div>
    </div>



    <!-- 主展示区 -->
    <div class="main-content">
      <div class="background-pattern"></div>
      <div class="container">
        <div class="left-section">


          <!-- <div class="illustration">
            <img src="@/assets/img/upup.png" alt="易学探索助手">
          </div> -->
        </div>
        <div class="right-section">
          <h1>探索易学智慧<br>&emsp;传承千年文化</h1>
          <p class="tagline">AI赋能，让易学研究更智能</p>
          <div class="button-group">
            <router-link to="/outerModel" class="action-button">
              <div class="button-icon">🤖</div>
              <div class="button-text">AI对话助手</div>
            </router-link>
            <router-link to="/book" class="action-button">
              <div class="button-icon">📜</div>
              <div class="button-text">古籍辅助阅读</div>
            </router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 特色功能展示 -->
    <div class="showcase">
      <h2>开始使用</h2>
      <div class="showcase-container">
        <div class="showcase-card">
          <div class="showcase-image">
            <img src="@/assets/img/img.png" alt="AI对话助手">
          </div>
          <div class="showcase-content">
            <h3>AI对话助手</h3>
            <p>基于先进的人工智能技术，能够回答各类易学相关问题，提供专业而通俗的解释。</p>
            <router-link to="/outerModel" class="showcase-button">开启对话</router-link>
          </div>
        </div>
        <div class="showcase-card">
          <div class="showcase-image">
            <img src="@/assets/img/guji.png" alt="古籍分析工具">
          </div>
          <div class="showcase-content">
            <h3>古籍分析工具</h3>
            <p>上传古籍图片或文本，AI自动识别并提供注释、翻译和结构分析。</p>
            <router-link to="/book" class="showcase-button">上传古籍</router-link>
          </div>
        </div>
        <div class="showcase-card">
          <div class="showcase-image">
            <img src="@/assets/img/tupu.png" alt="学术研究工具">
          </div>
          <div class="showcase-content">
            <h3>学术研究工具</h3>
            <p>提供易学研究所需的研究工具，包括文献学术翻译、知识图谱并支持导出。</p>
            <router-link to="/book" class="showcase-button">开始研究</router-link>
          </div>
        </div>
      </div>
    </div>


    <!-- 推荐内容 -->
    <div class="recommendations">
      <h2>精选内容</h2>
      <div class="recommendation-slider">
        <div class="recommendation-card">
          <img src="@/assets/img/book.jpg" alt="易经入门">
          <div class="recommendation-content">
            <h3>易经入门：初学者的向导</h3>
            <p>从一篇文章切入，与您聊聊《太上老君说常清静经》</p>
            <router-link to="/article_1" target="_blank" class="recommendation-button">阅读文章</router-link>
          </div>
        </div>
        <div class="recommendation-card">
          <img src="@/assets/img/zhouyi_cover.jpg" alt="周易研究">
          <div class="recommendation-content">
            <h3>周易研究方法论</h3>
            <p>现代学术视角下的《周易》研究方法与实践指南</p>
            <router-link to="/article_2" target="_blank" class="recommendation-button">阅读文章</router-link>
          </div>
        </div>
        <!--        <div class="recommendation-card">-->
        <!--          <img src="@/assets/img/yj.png" alt="工具下载">-->
        <!--          <div class="recommendation-content">-->
        <!--            <h3>易学研究专用工具包</h3>-->
        <!--            <p>集合多种易学研究工具的软件包，提升研究效率</p>-->
        <!--            <router-link to="/download" class="recommendation-button">下载工具包</router-link>-->
        <!--          </div>-->
        <!--        </div>-->
        <div class="recommendation-card">
          <img src="@/assets/img/hero-img.png" alt="社区活动">
          <div class="recommendation-content">
            <h3>易州龙兴观道德经碑本</h3>
            <p>从易州龙兴观道德经碑本看老子《道德经》</p>
            <router-link to="/article_3" target="_blank" class="recommendation-button">阅读文章</router-link>
          </div>
        </div>
      </div>
    </div>

    <!-- 页脚 -->
    <footer class="footer">
      <div class="footer-content">
        <div class="footer-section">
          <h3>关于我们</h3>
          <p>ITAN易学探索助手致力于利用现代人工智能技术，帮助全球易学爱好者和研究者探索这一古老智慧。</p>
        </div>
        <div class="footer-section">
          <h3>联系方式</h3>
          <p>邮箱: contact@itan.com</p>
          <p>电话: (123) 456-7890</p>
        </div>
        <div class="footer-section">
          <h3>关注我们</h3>
          <div class="social-links">
            <a href="#" class="social-link">微信</a>
            <a href="#" class="social-link">微博</a>
            <a href="#" class="social-link">知乎</a>
            <a href="#" class="social-link">B站</a>
          </div>
        </div>
        <div class="footer-section">
          <h3>法律信息</h3>
          <div class="legal-links">
            <router-link to="/terms" class="legal-link">使用条款</router-link>
            <router-link to="/privacy" class="legal-link">隐私政策</router-link>
            <router-link to="/copyright" class="legal-link">版权声明</router-link>
          </div>
        </div>
      </div>
      <div class="copyright">
        <p>&copy; 2025 ITAN. All rights reserved.</p>
      </div>
    </footer>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onBeforeUnmount } from 'vue';
import request from "../utils/request"
import router from "@/router/index.js";


//返回首页
const usage = () => {
  router.push('/usage')
}

//用户登录与退出
const nickname = ref('用户')
const isLoggedIn = ref(false)

onMounted(() => {
  const storedNickname = localStorage.getItem('nickname')
  const storedToken = localStorage.getItem('token')
  if (storedToken && storedNickname) {
    nickname.value = storedNickname
    isLoggedIn.value = true
  } else {
    nickname.value = '用户'
    isLoggedIn.value = false
  }
})

const logout = () => {
  console.log("退出登录")
  localStorage.removeItem('token')
  localStorage.removeItem('userId')
  localStorage.removeItem('nickname')
  isLoggedIn.value = false
  location.reload() // 可选刷新当前页
}
const goLogin = () => {
  router.push('/login')
}

const goRegister = () => {
  router.push('/register')
}

//滚动
function handleScroll() {
  const topbar = document.querySelector('.topbar')
  if (window.scrollY > 80) {
    topbar?.classList.add('show')
  } else {
    topbar?.classList.remove('show')
  }
}

onMounted(() => {
  window.addEventListener('scroll', handleScroll)
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})

//修改个人信息
const toUser = () => {
  router.push('/userInform')
}


</script>
<style scoped>
.itan-landing-page {
  font-family: 'Noto Serif SC', serif;
  background-color: #FAF0E6;
  /* 象牙白背景 */
  color: #FFFFFF;
  overflow-x: hidden;
}

/* 顶部导航栏 */

.topbar.show {
  top: 0;
  /* 显示时回到顶部 */
}

.topbar {
  position: fixed;
  top: -80px;
  /* 初始隐藏，往上偏移高度 */
  transition: top 0.4s ease;
  left: 0;
  width: 100%;
  height: 60px;
  line-height: 60px;
  background: linear-gradient(90deg, #f3e9e4, #fff8f5);
  color: #5b3a33;
  box-shadow: 0 2px 8px rgba(91, 58, 51, 0.1);
  backdrop-filter: saturate(180%) blur(4px);
  -webkit-backdrop-filter: saturate(180%) blur(4px);
  border-bottom: 1px solid #d9cfc7;
  z-index: 1000;
}


.topbar-content {
  display: flex;
  justify-content: space-between;
  /* 改为两端对齐 */
  align-items: center;
  height: 100%;
  padding: 0 20px;
}

.logo-section {
  display: flex;
  align-items: center;
  gap: 10px;
  flex: 0 0 auto;
  /* 禁止伸缩 */
}

.logo-section img {
  height: 38px;
  filter: drop-shadow(2px 2px 4px rgba(0, 0, 0, 0.1));
}

.logo-text {
  font-size: 1.3rem;
  font-weight: 700;
  color: #5b3a33;
  font-family: 'Noto Serif SC', serif;
}

.nav-links {
  display: flex;
  gap: 20px;
  flex: 1;
  /* 占据剩余空间 */
  justify-content: center;
  /* 居中排列按钮 */
  margin: 0 20px;
  /* 添加边距防止拥挤 */
}

.nav-links::-webkit-scrollbar {
  display: none;
}

.navbar-user-info {
  display: flex;
  gap: 12px;
  flex: 0 0 auto;
  /* 禁止伸缩 */
}

.nav-link {
  background-color: rgba(91, 58, 51, 0.05);
  color: #5b3a33;
  border: 1px solid #d9cfc7;
  padding: 6px 16px;
  /* 调整内边距 */
  line-height: 1.5;
  /* 固定行高 */
  height: 34px;
  /* 固定高度 */
  display: inline-flex;
  /* 使用弹性布局 */
  align-items: center;
  /* 垂直居中 */
  margin: auto 0;
  /* 垂直居中 */
}

.nav-link:hover {
  background-color: rgba(91, 58, 51, 0.1);
  transform: translateY(-1px);
}


/*
.search-bar {
  display: flex;
  align-items: center;
  background-color: rgba(255, 255, 255, 0.15);
  border-radius: 20px;
  padding: 5px 15px;
  margin-left: 20px;
}

.search-bar input {
  background: transparent;
  border: none;
  outline: none;
  color: #FFFFFF;
  padding: 5px;
  width: 180px;
}

.search-button {
  background: transparent;
  border: none;
  color: #FFFFFF;
  cursor: pointer;
  font-size: 1.1rem;
}
*/



/* 主展示区 */
/* .main-content {
  position: relative;
  padding: 180px 0;
  background: linear-gradient(to bottom, #8B2323, #A52A2A);
  overflow: hidden;
} */
.main-content {
  position: relative;
  padding: 180px 0;
  background: url('@/assets/img/cover.gif') center/cover no-repeat;
  overflow: hidden;
  min-height: 100vh;
}


.background-pattern {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  background:
    radial-gradient(circle at 10% 10%, rgba(255, 255, 255, 0.05) 0%, transparent 50%),
    radial-gradient(circle at 90% 90%, rgba(255, 255, 255, 0.05) 0%, transparent 50%);
  opacity: 0.8;
  z-index: -1;
}

.container {
  display: flex;
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
}

.left-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: flex-start;
  padding: 50px;
  position: relative;
  z-index: 1;
}



.right-section h1 {
  font-size: 2.8rem;
  margin-bottom: 15px;
  color: #CD5C5C;
  font-weight: 700;
  text-shadow: 0 0 15px rgba(255, 255, 255, 0.5);
  letter-spacing: 1px;
}

.right-section h1 i {
  display: inline-block;
  width: 100%;
}

.tagline {
  font-size: 1.2rem;
  margin-bottom: 30px;
  color: #000000;
  font-weight: 300;
}

.quick-access-buttons {
  display: flex;
  gap: 15px;
  margin-bottom: 25px;
  flex-wrap: wrap;
}

.quick-access-button {
  background-color: rgba(0, 0, 0, 0.2);
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 20px;
  padding: 10px 15px;
  display: flex;
  align-items: center;
  gap: 8px;
  text-decoration: none;
  color: #FFFFFF;
  transition: all 0.3s;
}

.quick-access-button:hover {
  background-color: rgba(0, 0, 0, 0.3);
  transform: translateY(-3px);
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2);
}

.quick-icon {
  font-size: 1.2rem;
  color: #FFFFFF;
}

.quick-text {
  font-weight: 500;
}

.illustration img {
  max-width: 100%;
  height: auto;
  filter: drop-shadow(0 10px 15px rgba(0, 0, 0, 0.3));
  transition: transform 0.5s;
}

.illustration img:hover {
  transform: scale(1.05);
}

.right-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  padding: 20px;
  position: relative;
}

.button-group {
  display: grid;
  grid-template-columns: 1fr 1fr;
  grid-gap: 20px;
  width: 100%;
  max-width: 500px;
}

.action-button {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 10px;
  background-color: #faf0e600;
  color: #000000;
  border: 1px solid rgba(255, 255, 255, 0.2);
  border-radius: 12px;
  cursor: pointer;
  text-decoration: none;
  transition: all 0.3s;
  /* box-shadow: 0 5px 15px rgba(0, 0, 0, 0.2); */
}

.action-button:hover {
  background-color: rgba(0, 0, 0, 0.3);
  transform: translateY(-5px);
  box-shadow: 0 10px 20px rgba(0, 0, 0, 0.3);
}

.button-icon {
  font-size: 2.2rem;
  margin-bottom: 15px;
  color: #FFFFFF;
}

.button-text {
  font-weight: 500;
}

/* 特色功能展示 */
.showcase {
  margin: 80px auto;
  max-width: 1200px;
  padding: 0 20px;
  text-align: center;
}

.showcase h2 {
  font-size: 2.2rem;
  color: #8B2323;
  margin-bottom: 40px;
  position: relative;
  display: inline-block;
}

.showcase h2::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background-color: #8B2323;
}

.showcase-container {
  display: flex;
  gap: 30px;
  overflow-x: auto;
  padding: 20px 0;
  scroll-behavior: smooth;
}

.showcase-container::-webkit-scrollbar {
  height: 8px;
}

.showcase-container::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.showcase-container::-webkit-scrollbar-thumb {
  background: #8B2323;
  border-radius: 10px;
}

.showcase-card {
  flex: 0 0 350px;
  background-color: #FFFFFF;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  border: 1px solid #8B2323;
}

.showcase-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
}

.showcase-image {
  height: 200px;
  overflow: hidden;
}

.showcase-image img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.5s;
}

.showcase-card:hover .showcase-image img {
  transform: scale(1.1);
}

.showcase-content {
  padding: 25px 20px;
  text-align: left;
}

.showcase-content h3 {
  font-size: 1.4rem;
  margin-bottom: 15px;
  color: #8B2323;
}

.showcase-content p {
  margin-bottom: 20px;
  color: #555;
  line-height: 1.6;
}

.showcase-button {
  display: inline-block;
  padding: 8px 20px;
  background-color: #8B2323;
  color: #FFFFFF;
  border-radius: 5px;
  text-decoration: none;
  transition: all 0.3s;
}

.showcase-button:hover {
  background-color: #A52A2A;
  transform: translateY(-2px);
}

/* 功能特点 */
.features {
  margin: 80px auto;
  max-width: 1200px;
  padding: 0 20px;
}

.features h2 {
  text-align: center;
  margin-bottom: 40px;
  font-size: 2.2rem;
  color: #8B2323;
}

.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
  gap: 30px;
}

.feature-card {
  background-color: #FFFFFF;
  border-radius: 12px;
  padding: 30px 20px;
  box-shadow: 0 5px 15px rgba(0, 0, 0, 0.1);
  text-align: center;
  transition: all 0.3s;
  border: 1px solid #8B2323;
}

.feature-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 30px rgba(0, 0, 0, 0.2);
}

.feature-icon {
  font-size: 3rem;
  margin-bottom: 20px;
  color: #8B2323;
}

.feature-card h3 {
  margin-bottom: 15px;
  font-size: 1.3rem;
  color: #8B2323;
}

.feature-card p {
  color: #555;
  line-height: 1.6;
}

/* 推荐内容 */
.recommendations {
  margin: 80px auto;
  max-width: 1200px;
  padding: 0 20px;
  text-align: center;
}

.recommendations h2 {
  font-size: 2.2rem;
  color: #8B2323;
  margin-bottom: 40px;
  position: relative;
  display: inline-block;
}

.recommendations h2::after {
  content: '';
  position: absolute;
  bottom: -10px;
  left: 50%;
  transform: translateX(-50%);
  width: 60px;
  height: 3px;
  background-color: #8B2323;
}

.recommendation-slider {
  display: flex;
  gap: 20px;
  overflow-x: auto;
  padding: 20px 0;
  scroll-behavior: smooth;
  justify-content: center;
  /* 添加此行 */
}

.recommendation-slider::-webkit-scrollbar {
  height: 8px;
}

.recommendation-slider::-webkit-scrollbar-track {
  background: #f1f1f1;
  border-radius: 10px;
}

.recommendation-slider::-webkit-scrollbar-thumb {
  background: #8B2323;
  border-radius: 10px;
}

.recommendation-card {
  flex: 0 0 300px;
  background-color: #FFFFFF;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 10px 25px rgba(0, 0, 0, 0.1);
  transition: all 0.3s;
  border: 1px solid #8B2323;
}

.recommendation-card:hover {
  transform: translateY(-10px);
  box-shadow: 0 15px 35px rgba(0, 0, 0, 0.2);
}

.recommendation-card img {
  width: 100%;
  height: 180px;
  object-fit: cover;
}

.recommendation-content {
  padding: 20px;
  text-align: left;
}

.recommendation-content h3 {
  font-size: 1.3rem;
  margin-bottom: 10px;
  color: #8B2323;
}

.recommendation-content p {
  margin-bottom: 15px;
  color: #555;
  line-height: 1.5;
}

.recommendation-button {
  display: inline-block;
  padding: 8px 16px;
  background-color: #8B2323;
  color: #FFFFFF;
  border-radius: 5px;
  text-decoration: none;
  transition: all 0.3s;
}

.recommendation-button:hover {
  background-color: #A52A2A;
  transform: translateY(-2px);
}

/* 页脚 */
.footer {
  background-color: #2C3E50;
  color: rgba(255, 255, 255, 0.8);
  padding: 50px 0 20px;
  margin-top: 50px;
}

.footer-content {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 20px;
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 30px;
}

.footer-section h3 {
  color: #FFFFFF;
  margin-bottom: 20px;
  font-size: 1.2rem;
  position: relative;
}

.footer-section h3::after {
  content: '';
  position: absolute;
  bottom: -8px;
  left: 0;
  width: 40px;
  height: 2px;
  background-color: #8B2323;
}

.footer-section p {
  line-height: 1.6;
  margin-bottom: 10px;
}

.social-links {
  display: flex;
  gap: 15px;
}

.social-link {
  color: rgba(255, 255, 255, 0.8);
  text-decoration: none;
  transition: all 0.3s;
}

.social-link:hover {
  color: #8B2323;
}

.legal-links {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.legal-link {
  color: rgba(255, 255, 255, 0.7);
  text-decoration: none;
  transition: all 0.3s;
  display: inline-block;
}

.legal-link:hover {
  color: #8B2323;
  transform: translateX(5px);
}

.copyright {
  text-align: center;
  padding-top: 30px;
  margin-top: 30px;
  border-top: 1px solid rgba(255, 255, 255, 0.1);
}

/* 响应式设计 */
@media (max-width: 992px) {
  .button-group {
    grid-template-columns: 1fr;
  }
}

@media (max-width: 768px) {
  .header-content {
    flex-wrap: wrap;
  }

  .nav-links {
    order: 3;
    width: 100%;
    justify-content: center;
    margin-top: 15px;
  }

  .container {
    flex-direction: column;
  }

  .left-section {
    align-items: center;
    text-align: center;
  }

  .left-section::after {
    left: 50%;
    transform: translateX(-50%);
  }

  .search-bar {
    margin-left: 0;
  }
}
</style>