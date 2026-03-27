export const appSettings = {
  appName: '光伏管理系统',
  microAppName: 'pvms-solar',
  version: '0.0.1',
  description: '面向虚拟电厂聚合商的生产监控、告警与运营分析前端子应用',
  menus: [
    {
      key: 'overview',
      label: '综合监控中心',
      path: '/dashboard/overview',
      icon: 'el-icon-data-analysis',
      description: '展示 GIS 电站总览、经营 KPI、功率偏差、发电排名和实时告警'
    },
    {
      key: 'production-monitor',
      label: '生产监控层',
      path: '/production-monitor/overview',
      icon: 'el-icon-s-operation',
      description: '以虚拟电厂聚合商视角查看资源总览、电站监控、负荷与出力和调度执行'
    },
    {
      key: 'forecast',
      label: '预测与分析',
      path: '/forecast/overview',
      icon: 'el-icon-s-data',
      description: '光伏功率预测、可调能力分析与精度评估'
    },
    {
      key: 'devices',
      label: '设备监控',
      path: '/devices/monitor',
      icon: 'el-icon-cpu',
      description: '跟踪关键设备状态和运行分布'
    },
    {
      key: 'alarms',
      label: '告警中心',
      path: '/alarms/center',
      icon: 'el-icon-warning-outline',
      description: '管理告警级别、处理进度与闭环结果'
    }
  ]
}

export default appSettings
