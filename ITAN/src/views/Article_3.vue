<template>
    <div class="container">
        <div class="header">
            <h1>易州龙兴观道德经碑本</h1>
        </div>

        <div class="content">
            <section class="scripture section">
                <h2>碑本正文</h2>
                <div v-for="(paragraph, index) in scripture" :key="index" class="paragraph">
                    <p>{{ paragraph }}</p>
                </div>
            </section>

        </div>

        <div class="footer">
            <p>© 2025 易学经典传承 | 版本 1.0.0</p>
        </div>
    </div>
</template>

<script setup>

const scripture = [
    "钱大昕曰：案河上公注本“道可道”以下为道经卷上，“上德不德”以下为德经卷下。晁说之跋王弼注本，谓其不析道德而上下之，犹近于古。不知陆德明所撰释文，正用辅嗣本，题云“道经卷上”，“德经卷下”，与河上本不异。晁氏所见者，特宋时转写之本，而翻以为近古，亦未之考矣。予家藏石刻道德经凡五本，惟明皇御注本及此本，皆分道经、德经为二，盖汉、魏以来篇目如此。而此本为初唐所刻，字句与他本多异。如“无”作“无”，“愈”作“俞”，“芸”作“云”，“誉”作“豫”，“荒”作“忙”，“佐”作“作”，“◆”作“翕”之类，皆从古字。又如“故能蔽不新成”，石本作“能蔽复成”。“师之所处，荆棘生”下，石本无“大军之后，必有凶年”二句。“上将军居右”下，石本无“言以丧礼处之”句。“夫唯病病，是以不病；圣人不病，以其病病，是以不病”，石本但云：“是以圣人不病，以其病病，是以不病。”此类皆远胜他本，聊举一二，以见古石刻之可贵也。武亿曰：分老子道经卷上，德经卷下，亦与古本相彷。",
    "后陆放翁题跋云：“晁以道谓王辅嗣老子曰：‘道德经不析乎道德而上下之，犹近于古。’今此本已久离析。”然则宋已失辅嗣定本。今邢氏论语疏引老子德经云：“天网恢恢，疏而不失。”此其可征之一也。然又考汉书注，如颜氏于魏豹传，引老子道经曰：“国家昏乱有忠臣。”田横传引老子德经曰：“贵以贱为本，高以下为基，是以侯王自谓孤、寡、不谷。”楚元王传引老子德经云：“知足不辱。”严助传：老子所谓“师之所处，荆棘生之”者也。师古曰：“老子道经之言也。”扬雄传“贵知我者希”，师古曰：老子德经云“知我者希，则我贵矣”。酷吏传老氏称“上德不德，是以有德；下德不失德，是以无德。法令滋章，盗贼多有”。师古曰：“老子德经之言也。”“下士闻道，大笑之”，师古曰：“老子道经之言也。”　　西域传注：老子德经曰“天下有道，却走马以为粪”。盖其所引以道、德分篇者若此，而与释文题“道经音义”、“德经音义”者并合。又贾公彦周礼师氏疏，亦以为老子道经云：“道可道，非常道。”其下案德经云：“上德不德，是以有德。”章怀太子注后汉书，其于翟传也，则又谓老子道经曰：“鱼不可以脱于泉。”是数子于初唐时，并同所证。（梦真客碑：“稽之道经，以慈为宝。”）其必袭自晋、宋旧本，如此碑所分题，固有据也。",
    "王昶曰：碑上卷题“老子道经”，下卷题“老子德经”，皆道、德分见，未尝混而为一，则玄宗所注，实从古本如此。董迥藏书志谓“玄宗注成，始改定章句为道德经，凡言道者类之上卷，言德者类之下卷”，非也。",
    "吴云曰：隋书经籍志载道德经二卷，王弼注。晁说之、熊克重跋，皆称不分道德经，而今本释文实分上下二卷，或疑为刻者增入。然邢论语疏引老子德经“天网恢恢”二句，颜师古汉书注多引老子道经、德经，分之者当不自陆德明始。此石亦书德经，殆有据也。　　孙诒让曰：老子上下篇八十一章，分题“道经”、“德经”。河上公本，经典释文所载王注本，道藏唐傅奕校本，石刻唐玄宗注本并同。",
    "弘明集牟子理惑论云：“所理正于三十七条，兼法老氏道经三十七篇。”则汉时此书已分道、德二经，其道经三十七章，德经四十四章，亦与今本正同。今所传王注，出于宋晁说之所校，不分道、德二经，于义虽通，然非汉、唐故书之旧。",

];

</script>

<style scoped>
.container {
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

.content {
    margin-bottom: 40px;
}

.section {
    background-color: white;
    padding: 25px;
    border-radius: 8px;
    box-shadow: 0 2px 8px rgba(139, 0, 0, 0.1);
    margin-bottom: 30px;
}

.paragraph {
    line-height: 1.8;
    margin: 20px 0;
    text-indent: 2em;
    color: #5a4a4a;
}

.note {
    position: relative;
    padding-left: 40px;
    margin-bottom: 25px;
}

.note::before {
    content: "◆";
    position: absolute;
    left: 0;
    top: 0;
    color: #8b0000;
    font-size: 24px;
}

.annotations {
    counter-reset: section-counter;
}

.annotations h2::before {
    content: counter(section-counter);
    counter-increment: section-counter;
    display: inline-block;
    width: 30px;
    height: 30px;
    line-height: 30px;
    background-color: #8b0000;
    color: white;
    border-radius: 50%;
    margin-right: 10px;
    text-align: center;
}

.footer {
    text-align: center;
    margin-top: 40px;
    padding-top: 20px;
    border-top: 1px solid #e8c4c4;
    color: #666;
}
</style>