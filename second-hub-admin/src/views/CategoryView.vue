<template>
  <el-card>
    <template #header>分类管理</template>
    <div style="margin-bottom: 16px; display: flex; gap: 8px">
      <el-input v-model="form.name" placeholder="分类名" style="width: 200px" />
      <el-input-number v-model="form.sort" :min="0" />
      <el-select v-model="form.status" style="width: 120px">
        <el-option :value="1" label="启用" />
        <el-option :value="0" label="停用" />
      </el-select>
      <el-button type="primary" @click="save">新增</el-button>
    </div>

    <el-table :data="list" border>
      <el-table-column prop="id" label="ID" width="80" />
      <el-table-column prop="name" label="分类名" />
      <el-table-column prop="sort" label="排序" width="100" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="scope">{{ scope.row.status === 1 ? '启用' : '停用' }}</template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="scope">
          <el-button size="small" type="danger" @click="remove(scope.row.id)">删除</el-button>
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
const form = reactive({ name: '', sort: 0, status: 1 })

const load = async () => {
  list.value = await request.get('/api/admin/categories')
}

const save = async () => {
  if (!form.name) return
  await request.post('/api/admin/categories', form)
  form.name = ''
  ElMessage.success('保存成功')
  load()
}

const remove = async (id) => {
  await request.delete(`/api/admin/categories/${id}`)
  ElMessage.success('删除成功')
  load()
}

onMounted(load)
</script>
