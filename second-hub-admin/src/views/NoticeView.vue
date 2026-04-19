<template>
  <el-card>
    <template #header>公告管理</template>
    <div style="display: flex; gap: 8px; margin-bottom: 16px">
      <el-input v-model="form.title" placeholder="标题" style="width: 180px" />
      <el-input v-model="form.coverUrl" placeholder="封面URL" />
      <el-button type="primary" @click="save">发布</el-button>
    </div>
    <el-input v-model="form.content" type="textarea" :rows="3" placeholder="公告内容" style="margin-bottom: 16px" />

    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="title" label="标题" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">{{ scope.row.status === 1 ? '发布' : '下线' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="scope">
          <el-button type="danger" size="small" @click="remove(scope.row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '../utils/request'

const list = ref([])
const form = reactive({ title: '', content: '', coverUrl: '', status: 1 })

const load = async () => {
  const page = await request.get('/api/admin/notices', { params: { pageNo: 1, pageSize: 100 } })
  list.value = page.records || []
}

const save = async () => {
  if (!form.title || !form.content) return
  await request.post('/api/admin/notices', form)
  form.title = ''
  form.content = ''
  form.coverUrl = ''
  ElMessage.success('保存成功')
  load()
}

const remove = async (id) => {
  await request.delete(`/api/admin/notices/${id}`)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
