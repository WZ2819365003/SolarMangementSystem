import VueRouter from 'vue-router'
import ShellLayout from '@/layouts/ShellLayout.vue'
import OverviewPage from '@/modules/dashboard/pages/OverviewPage.vue'
import DeviceMonitorPage from '@/modules/devices/pages/DeviceMonitorPage.vue'
import AlarmCenterPage from '@/modules/alarms/pages/AlarmCenterPage.vue'
import ProductionMonitorPage from '@/modules/production-monitor/pages/ProductionMonitorPage.vue'
const ForecastPage = () => import('@/modules/forecast/pages/ForecastPage.vue')
const StrategyPage = () => import('@/modules/strategy/pages/StrategyPage.vue')

const routes = [
  {
    path: '/',
    component: ShellLayout,
    redirect: '/dashboard/overview',
    children: [
      {
        path: '/production-monitor/overview',
        name: 'production-monitor-overview',
        component: ProductionMonitorPage,
        meta: {
          title: '资源总览',
          section: '生产监控层',
          summary: '在统一模块内查看资源单元健康、可调能力、天气快报和成员电站概况',
          activeMenu: '/production-monitor/overview',
          viewKey: 'overview'
        }
      },
      {
        path: '/production-monitor/output',
        name: 'production-monitor-output',
        component: ProductionMonitorPage,
        meta: {
          title: '出力分析',
          section: '生产监控层',
          summary: '查看同区位资源单元的预测与实际出力曲线、天气变化和成员电站贡献',
          activeMenu: '/production-monitor/overview',
          viewKey: 'output'
        }
      },
      {
        path: '/production-monitor/dispatch',
        name: 'production-monitor-dispatch',
        component: ProductionMonitorPage,
        meta: {
          title: '调度执行',
          section: '生产监控层',
          summary: '查看当日指令执行、响应时间、执行成功率和偏差风险提示',
          activeMenu: '/production-monitor/overview',
          viewKey: 'dispatch'
        }
      },
      {
        path: '/production-monitor/station',
        name: 'production-monitor-station',
        component: ProductionMonitorPage,
        meta: {
          title: '电站监控',
          section: '生产监控层',
          summary: '以树状结构查看单站实时数据：可调空间、光伏出力、负荷监控和出力预测',
          activeMenu: '/production-monitor/overview',
          viewKey: 'station'
        }
      },
      {
        path: '/production-monitor/load',
        name: 'production-monitor-load',
        component: ProductionMonitorPage,
        meta: {
          title: '负荷与出力',
          section: '生产监控层',
          summary: '查看负荷与光伏出力对比，分析可调空间变化',
          activeMenu: '/production-monitor/overview',
          viewKey: 'load'
        }
      },
      {
        path: '/station',
        redirect: '/production-monitor/overview'
      },
      {
        path: '/station/:id',
        redirect: '/production-monitor/overview'
      },
      {
        path: '/stations/archive',
        redirect: '/production-monitor/station'
      },
      {
        path: '/forecast/overview',
        name: 'forecast-overview',
        component: ForecastPage,
        meta: {
          title: '预测总览',
          section: '预测与分析',
          summary: '查看日前与超短期光伏功率预测对比、偏差热力图和电站预测明细',
          activeMenu: '/forecast/overview',
          viewKey: 'overview'
        }
      },
      {
        path: '/forecast/adjustable',
        name: 'forecast-adjustable',
        component: ForecastPage,
        meta: {
          title: '可调能力分析',
          section: '预测与分析',
          summary: '查看24小时可调能力预测曲线、VPP节点可调时间线和电站可调明细',
          activeMenu: '/forecast/overview',
          viewKey: 'adjustable'
        }
      },
      {
        path: '/forecast/accuracy',
        name: 'forecast-accuracy',
        component: ForecastPage,
        meta: {
          title: '精度评估',
          section: '预测与分析',
          summary: '评估预测精度趋势、偏差分布和电站精度排名',
          activeMenu: '/forecast/overview',
          viewKey: 'accuracy'
        }
      },
      {
        path: '/strategy/list',
        name: 'strategy-list',
        component: StrategyPage,
        meta: {
          title: '策略列表',
          section: '策略管理',
          summary: '查看策略台账、状态流转、收益快照和批量操作结果',
          activeMenu: '/strategy/list',
          viewKey: 'list'
        }
      },
      {
        path: '/strategy/config',
        name: 'strategy-config',
        component: StrategyPage,
        meta: {
          title: '策略配置',
          section: '策略管理',
          summary: '配置站点策略、查看树结构、电价窗口并执行模拟和创建',
          activeMenu: '/strategy/list',
          viewKey: 'config'
        }
      },
      {
        path: '/strategy/revenue',
        name: 'strategy-revenue',
        component: StrategyPage,
        meta: {
          title: '收益分析',
          section: '策略管理',
          summary: '查看策略收益趋势、明细拆分和策略对比结果',
          activeMenu: '/strategy/list',
          viewKey: 'revenue'
        }
      },
      {
        path: '/dashboard/overview',
        name: 'dashboard-overview',
        component: OverviewPage,
        meta: {
          title: '综合监控中心',
          section: '首页驾驶舱',
          summary: '聚合 GIS 电站总览、经营指标、功率偏差、发电排名与实时告警'
        }
      },
      {
        path: '/devices/monitor',
        name: 'devices-monitor',
        component: DeviceMonitorPage,
        meta: {
          title: '设备监控',
          section: '设备管理',
          summary: '跟踪关键设备实时状态和异常分布'
        }
      },
      {
        path: '/alarms/center',
        name: 'alarms-center',
        component: AlarmCenterPage,
        meta: {
          title: '告警中心',
          section: '告警处置',
          summary: '统一查看告警列表、级别和处理进度'
        }
      }
    ]
  },
  {
    path: '*',
    redirect: '/dashboard/overview'
  }
]

export default function createRouter(base = '/') {
  return new VueRouter({
    mode: 'history',
    base,
    routes
  })
}
