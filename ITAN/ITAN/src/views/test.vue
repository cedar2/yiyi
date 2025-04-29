<template>
  <div>
    <div>
      <input v-model="question" type="text" placeholder="输入内容" />
      <button @click="fetchData2">打印输入</button>
      <div class="response-box">
        <span class="response-box" v-for="(line, index) in responseData" :key="index">{{ line }}</span>
      </div>
    </div>
    <div>
      <div>
        <input v-model="question2" type="text" placeholder="输入内容" />
      </div>
      <div>
        <button @click="fetchGraphData" class="load-btn">显示知识图谱</button>
      </div>
      <div ref="chartRef" class="chart-box" v-show="chartVisible"></div>
    </div>
  </div>

  <!-- <RouterView /> -->
</template>

<script>
import axios from 'axios';
import { ref } from 'vue'
import * as echarts from 'echarts'
export default {
  data() {
    return {
      question: '',
      question2: '',
      responseData: [],
      chart: null,
      chartVisible: false,
    };
  },
  methods: {
    logInput() {
      console.log(this.inputValue);
    },
    async fetchData() {
      this.responseData = [];
      try {
        const response = await axios.post(
            '/api/qwen14b',
            { question: this.question },
            { headers: { 'Content-Type': 'application/json' }
              , responseType: 'stream'
            }
        );
        // const reader = response.data.getReader();
        console.log(response);
        // let decoder = new TextDecoder();
        // while (true) {
        //   const { done, value } = await reader.read();
        //   if (done) break;
        //   let text = decoder.decode(value, { stream: true });
        //   text.split('\n').forEach(line => {

        //     if (line.startsWith('data: ')) {
        //       this.responseData.push(line.replace('data: ', ''));
        //     }
        //   });
        // }
      } catch (error) {
        console.error('请求失败', error);
      }
    },

    async fetchData2() {
      this.responseData = [];
      try {
        const response = await fetch("/qwen32b", {
          method: "POST",
          headers: {
            "Content-Type": "application/json",
          },
          body: JSON.stringify({ question: this.question }),
        });

        if (!response.body) {
          throw new Error("服务器未返回流式数据");
        }

        const reader = response.body.getReader();
        const decoder = new TextDecoder();

        while (true) {
          const { done, value } = await reader.read();
          if (done) break;
          const text = decoder.decode(value, { stream: true });

          text.split("\n").forEach((line) => {
            if (line.startsWith("data: ")) {
              line = line.replace("\\n", "\n");
              line = line.replace("\\n", "");
              this.responseData.push(line.replace("data: ", ""));
              //console.log(line.replace("data: ", ""));
            }
          });
        }
      } catch (error) {
        console.error("请求失败", error);
      }
    },
    async fetchGraphData() {
      const response = await fetch('/getGraph', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          question: this.question2,
        }),
      })

      const result = await response.json()
      const entities = result.entities
      const add_entities = result.add_entities
      const combined_entities = [...entities,...add_entities]

      const links = result.relationships.map(item => ({
        source: parseInt(item[7]),
        target: parseInt(item[8]),
        label: item[4],
      }))

      const typeSet = [...new Set(result.entities.map(item => item[2]))]
      const colors = [  '#FF6666', // 红色
        '#3399FF', // 蓝色
        '#66CC99', // 绿色
        '#9966CC', // 紫色
        '#FFCC00', // 金黄
        '#FF9966', // 橙色
        '#66CCCC', // 青色
        '#CC99FF', // 淡紫
        '#FF6699', // 粉红
        '#99CC33', // 黄绿
        '#009999', // 深青
        '#CCCC66', // 土黄
        '#FFCC99', // 米黄
        '#6699CC', // 灰蓝
        '#CC6666', // 砖红
        '#9999CC', // 淡蓝紫
        '#99CCCC', // 冷青
        '#FF9999', // 桃红
        '#CCCCFF', // 淡蓝
        '#996633'  // 咖啡
      ]
      const categories = typeSet.map((item, index) => ({
        name: item,
        itemStyle: {
          color: colors[index % colors.length],
        },
      }))

      categories.push({
        name: '未知',
        itemStyle: {
          color: '#808080',
        },
      })

      const nodes = combined_entities.map(item => ({
        id: parseInt(item[0]),
        name: item[1],
        symbolSize: item[2]==='未知'? 30 : 50,
        category: item[2] === '未知'? categories.length - 1 : typeSet.indexOf(item[2]),
        tooltip: {
          formatter: `
          <div style="width: 300px; white-space: normal;">
            <strong>${item[1]}</strong><br/>
            类型: ${item[2]}<br/>
            描述: ${item[3]}
          </div>
        `
        },
        label: {
          show: item[2] !== '未知',
        }
      }))


      if (!this.chart) {
        this.chart = echarts.init(this.$refs.chartRef)
      }

      const option = {
        title: {
          text: '知识图谱',
          left: 'center',
        },
        tooltip: {
          formatter: (param) => {
            if (param.dataType === 'edge') {
              return `关系: ${param.data.label}`
            } else {
              return `实体: ${param.data.name}`
            }
          },
        },
        legend: [{
          data: categories.map(c => c.name)
        }],
        series: [
          {
            type: 'graph',
            layout: 'force',
            symbolSize: 50,
            roam: true,
            label: {
              show: true,
            },
            edgeLabel: {
              show: true,
              formatter: (x) => {
                const label = x.data.label || '';
                return label.length < 7 ? label : '';
              },
            },
            data: nodes,
            links: links,
            categories: categories,
            force: {
              repulsion: 200,
              edgeLength: [80, 200],
            },
          },
        ],
      }

      this.chart.setOption(option)
      this.chartVisible = true
    },
  },
};
</script>

<style scoped>
input {
  margin-right: 10px;
  padding: 5px;
}
button {
  padding: 5px 10px;
  cursor: pointer;
}
.response-box {
  white-space: pre-wrap;
}
.chart-box {
  width: 1000px;
  height: 600px;
  margin-top: 20px;
}
.load-btn {
  padding: 10px 20px;
  font-size: 16px;
  cursor: pointer;
}
</style>
