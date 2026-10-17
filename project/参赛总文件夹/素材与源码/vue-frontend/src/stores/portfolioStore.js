import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getUserAssetConfig, getUserStockHolds, getUserDetail } from '../api/userAsset'

export const usePortfolioStore = defineStore('portfolio', () => {
  const currentUserId = ref(1)

  const accountInfo = ref({
    total: 3500,
    todayProfit: 15.20,
    todayProfitRate: 0.43,
    available: 2540
  })

  const userAssetConfig = ref({
    stockRatio: 27,
    fundRatio: 0,
    bondRatio: 0,
    etfRatio: 0,
    derivativesRatio: 0,
    cashRatio: 73
  })

  const stockHolds = ref([])

  const holdings = ref([
    { symbol: '600036', name: '招商银行', quantity: 100, cost: 38.50, price: 38.50, profit: 0 },
    { symbol: '601398', name: '工商银行', quantity: 100, cost: 5.80, price: 5.80, profit: 0 },
    { symbol: '601288', name: '农业银行', quantity: 100, cost: 4.20, price: 4.20, profit: 0 }
  ])

  const totalProfit = computed(() => {
    return holdings.value.reduce((sum, h) => sum + h.profit, 0)
  })

  const totalProfitRate = computed(() => {
    const totalCost = holdings.value.reduce((sum, h) => sum + h.cost * h.quantity, 0)
    if (totalCost === 0) return 0
    return Number((((totalProfit.value / totalCost) * 100).toFixed(2)))
  })

  const pieChartData = computed(() => {
    return holdings.value.map((h, index) => {
      const colors = ['#1a365d', '#e53e3e', '#38a169', '#3182ce', '#805ad5']
      return {
        value: h.price * h.quantity,
        name: h.name,
        itemStyle: { color: colors[index % colors.length] }
      }
    })
  })

  const updateAccount = (newAccount) => {
    Object.assign(accountInfo.value, newAccount)
  }

  const updateHoldings = (newHoldings) => {
    holdings.value = newHoldings
  }

  const executeTrade = (stock, type, quantity, price) => {
    if (type === 'buy') {
      const cost = quantity * price
      if (cost > accountInfo.value.available) {
        return { success: false, message: '可用资金不足' }
      }
      accountInfo.value.available -= cost
      const existingHolding = holdings.value.find(h => h.symbol === stock.symbol)
      if (existingHolding) {
        const totalCost = existingHolding.cost * existingHolding.quantity + cost
        const totalQty = existingHolding.quantity + quantity
        existingHolding.cost = totalCost / totalQty
        existingHolding.quantity = totalQty
        existingHolding.price = price
        existingHolding.profit = Number(((existingHolding.price - existingHolding.cost) * existingHolding.quantity).toFixed(2))
      } else {
        holdings.value.push({
          symbol: stock.symbol,
          name: stock.name,
          quantity: quantity,
          cost: price,
          price: price,
          profit: 0
        })
      }
    } else {
      const income = quantity * price
      accountInfo.value.available += income
      const holding = holdings.value.find(h => h.symbol === stock.symbol)
      if (holding) {
        holding.quantity -= quantity
        if (holding.quantity <= 0) {
          holdings.value = holdings.value.filter(h => h.symbol !== stock.symbol)
        } else {
          holding.profit = Number(((holding.price - holding.cost) * holding.quantity).toFixed(2))
        }
      }
    }
    return { success: true }
  }

  const recharge = (amount) => {
    accountInfo.value.available += amount
    accountInfo.value.total += amount
  }

  const fetchUserAssetConfig = async (userId) => {
    try {
      const res = await getUserAssetConfig(userId)
      if (res.success && res.data) {
        userAssetConfig.value = res.data
      }
    } catch (error) {
      console.error('Failed to fetch user asset config:', error)
    }
  }

  const fetchUserStockHolds = async (userId) => {
    try {
      const res = await getUserStockHolds(userId)
      if (res.success && res.data) {
        stockHolds.value = res.data.map(item => ({
          symbol: item.stockCode,
          name: item.stockName,
          quantity: item.holdNum,
          cost: item.buyPrice,
          currentPrice: item.buyPrice,
          profit: 0,
          profitPercent: 0,
          changePercent: 0
        }))
      }
    } catch (error) {
      console.error('Failed to fetch user stock holds:', error)
    }
  }

  const fetchUserDetail = async (userId) => {
    try {
      const res = await getUserDetail(userId)
      if (res.success && res.data) {
        if (res.data.asset) {
          userAssetConfig.value = res.data.asset
        }
        if (res.data.stockHolds) {
          stockHolds.value = res.data.stockHolds.map(item => ({
            symbol: item.stockCode,
            name: item.stockName,
            quantity: item.holdNum,
            cost: item.buyPrice,
            currentPrice: null,
            profit: 0,
            profitPercent: 0,
            changePercent: 0
          }))
        }
      }
    } catch (error) {
      console.error('Failed to fetch user detail:', error)
    }
  }

  const updateHoldingPrices = (priceData) => {
    priceData.forEach(data => {
      const apiCode = data.code.replace(/^(sh|sz)/i, '')
      const hold = stockHolds.value.find(h => h.symbol === apiCode)
      if (hold) {
        if (!hold.cost || hold.cost === 0) {
          hold.cost = data.price
        }
        hold.currentPrice = data.price
        hold.changePercent = data.changePercent
        hold.profit = (data.price - hold.cost) * hold.quantity
        hold.profitPercent = hold.cost > 0 ? ((data.price - hold.cost) / hold.cost) * 100 : 0
      }
    })
  }

  const assetPieChartData = computed(() => {
    const config = userAssetConfig.value
    return [
      { value: config.stockRatio, name: '股票', itemStyle: { color: '#e53935' } },
      { value: config.fundRatio, name: '基金', itemStyle: { color: '#1e88e5' } },
      { value: config.bondRatio, name: '债券', itemStyle: { color: '#43a047' } },
      { value: config.etfRatio, name: 'ETF', itemStyle: { color: '#fb8c00' } },
      { value: config.derivativesRatio, name: '衍生品', itemStyle: { color: '#8e24aa' } },
      { value: config.cashRatio, name: '现金', itemStyle: { color: '#6d4c41' } }
    ].filter(item => item.value > 0)
  })

  return {
    currentUserId,
    accountInfo,
    userAssetConfig,
    stockHolds,
    holdings,
    totalProfit,
    totalProfitRate,
    pieChartData,
    assetPieChartData,
    updateAccount,
    updateHoldings,
    updateHoldingPrices,
    executeTrade,
    recharge,
    fetchUserAssetConfig,
    fetchUserStockHolds,
    fetchUserDetail
  }
})