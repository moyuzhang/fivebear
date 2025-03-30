import { MenuItem } from '@/types/menu'

export const menuItems: MenuItem[] = [
  {
    title: 'IP池管理',
    icon: 'el-icon-connection',
    path: '/ip-pool',
    children: [
      {
        title: 'IP列表',
        path: '/ip-pool/list',
        icon: 'el-icon-list'
      },
      {
        title: 'IP池设置',
        path: '/ip-pool/settings',
        icon: 'el-icon-setting'
      },
      {
        title: 'IP池状态',
        path: '/ip-pool/status',
        icon: 'el-icon-data-line'
      }
    ]
  }
] 