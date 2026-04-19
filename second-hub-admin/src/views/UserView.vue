<template>
  <el-card>
    <template #header>用户管理</template>
    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="nickname" label="昵称" />
      <el-table-column prop="phone" label="手机号" />
      <el-table-column prop="status" label="状态" width="120">
        <template #default="scope">{{ scope.row.status === 1 ? '正常' : '禁用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" type="warning" @click="setStatus(scope.row, 0)">禁用</el-button>
          <el-button size="small" type="success" @click="setStatus(scope.row, 1)">启用</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const list = ref([])

const load = async () => {
  const page = await request.get('/api/admin/users', { params: { pageNo: 1, pageSize: 100 } })
  list.value = page.records || []
}

const setStatus = async (row, status) => {
  await request.post(`/api/admin/users/${row.id}/status`, { status })
  ElMessage.success('状态已更新')
  load()
}

onMounted(load)
</script>
