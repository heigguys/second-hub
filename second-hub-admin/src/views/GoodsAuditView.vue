<template>
  <el-card>
    <template #header>商品审核</template>
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="price" label="价格" width="120" />
      <el-table-column prop="createdAt" label="发布时间" width="180" />
      <el-table-column label="操作" width="260">
        <template #default="scope">
          <el-button type="success" size="small" @click="audit(scope.row, true)">通过</el-button>
          <el-button type="warning" size="small" @click="audit(scope.row, false)">拒绝</el-button>
          <el-button type="danger" size="small" @click="offline(scope.row)">下架</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { onMounted, ref } from 'vue'
import request from '../utils/request'

const list = ref([])

const load = async () => {
  const page = await request.get('/api/admin/goods/pending', { params: { pageNo: 1, pageSize: 50 } })
  list.value = page.records || []
}

const audit = async (row, approved) => {
  let reason = ''
  if (!approved) {
    reason = await ElMessageBox.prompt('请输入拒绝原因', '审核拒绝').then((res) => res.value).catch(() => null)
    if (reason === null) {
      return
    }
  }
  await request.post(`/api/admin/goods/${row.id}/audit`, { approved, reason })
  ElMessage.success('操作成功')
  load()
}

const offline = async (row) => {
  await request.post(`/api/admin/goods/${row.id}/offline`)
  ElMessage.success('已下架')
  load()
}

onMounted(load)
</script>
