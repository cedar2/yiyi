import { defineStore } from 'pinia'
import { ref, reactive } from 'vue'

export const useTextStore = defineStore('text', () => {
    const originalText = ref('')
    const segmentedText = ref('')
    const translationResult = ref('')
// 创建响应式 translationResults，使其对属性赋值有响应性
    const _rawTranslationResults = {
        '大众版': '',
        '白话版': '',
        '学术版': ''
    }

    const translationResults = ref(_rawTranslationResults)

    return {
        originalText,
        segmentedText,
        translationResults,
        translationResult
    }
})
