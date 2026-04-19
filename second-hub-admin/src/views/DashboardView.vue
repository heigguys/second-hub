<template>
  <div>
    <el-row :gutter="16">
      <el-col :span="4" v-for="item in cards" :key="item.label">
        <el-card>
          <div>{{ item.label }}</div>
          <h2>{{ item.value }}</h2>
        </el-card>
      </el-col>
    </el-row>

    <el-card style="margin-top: 16px">
      <template #header>近7日趋势</template>
      <el-table :data="trend" border>
        <el-table-column prop="date" label="日期" />
        <el-table-column prop="userCount" label="新增用户" />
        <el-table-column prop="goodsCount" label="新增商品" />
        <el-table-column prop="orderCount" label="新增订单" />
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, onMounted, ref } from 'vue'
import request from '../utils/request'

const overview = ref({})
const trend = ref([])

const cards = computed(() => [
  { label: '用户总数', value: overview.value.userCount || 0 },
  { label: '商品总数', value: overview.value.goodsCount || 0 },
  { label: '待审核商品', value: overview.value.pendingGoodsCount || 0 },
  { label: '订单总数', value: overview.value.orderCount || 0 },
  { label: '待处理举报', value: overview.value.reportCount || 0 }
])

const load = async () => {
  overview.value = await request.get('/api/admin/dashboard/overview')
  trend.value = await request.get('/api/admin/dashboard/trend')
}

onMounted(load)
</script>
