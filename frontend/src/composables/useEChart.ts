import { nextTick, onBeforeUnmount, onMounted, shallowRef, type ShallowRef } from 'vue'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import { GridComponent, LegendComponent, TooltipComponent } from 'echarts/components'
import * as echarts from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import type { EChartsOption } from 'echarts'

echarts.use([
  BarChart,
  LineChart,
  PieChart,
  GridComponent,
  LegendComponent,
  TooltipComponent,
  CanvasRenderer,
])

export function useEChart(
  el: ShallowRef<HTMLElement | null>,
  getOption: () => EChartsOption,
  canRender: () => boolean
) {
  const chart = shallowRef<ReturnType<typeof echarts.init> | null>(null)

  async function render() {
    await nextTick()
    if (!el.value) return
    const instance = chart.value ?? echarts.init(el.value)
    chart.value = instance
    if (!canRender()) {
      instance.clear()
      return
    }
    instance.setOption(getOption(), true)
  }

  function resize() {
    chart.value?.resize()
  }

  onMounted(() => {
    window.addEventListener('resize', resize)
  })

  onBeforeUnmount(() => {
    window.removeEventListener('resize', resize)
    chart.value?.dispose()
    chart.value = null
  })

  return { render, resize }
}
