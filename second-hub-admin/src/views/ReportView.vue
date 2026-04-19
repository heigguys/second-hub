<template>
  <el-card>
    <template #header>举报处理</template>
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="goodsId" label="商品ID" width="100" />
      <el-table-column prop="reason" label="原因" />
      <el-table-column prop="status" label="状态" width="120" />
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" type="primary" @click="handle(scope.row)">处理</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '../utils/request'

const list = ref([])

const load = async () => {
  const page = await request.get('/api/admin/reports', { params: { pageNo: 1, pageSize: 100 } })
  list.value = page.records || []
}

const handle = async (row) => {
  const text = await ElMessageBox.prompt('请输入处理结果', '处理举报').then((res) => res.value).catch(() => null)
  if (text === null) return
  await request.post(`/api/admin/reports/${row.id}/handle`, { handleResult: text })
  ElMessage.success('处理成功')
  load()
}

onMounted(load)
</script>
