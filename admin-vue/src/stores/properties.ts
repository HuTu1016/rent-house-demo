import { defineStore } from 'pinia'
import { ref } from 'vue'

export type SavedBuilding = { id: string; name: string; address: string; createdAt: string }
export type SavedUnit = { id: string; buildingId: string; buildingName: string; unitNo: string; title: string; rooms: number; halls: number; bathrooms: number; areaSqm: number; createdAt: string }

const STORAGE_BUILDINGS_KEY = 'admin-saved-buildings'
const STORAGE_UNITS_KEY = 'admin-saved-units'

export const usePropertyStore = defineStore('properties', () => {
  const buildings = ref<SavedBuilding[]>(JSON.parse(localStorage.getItem(STORAGE_BUILDINGS_KEY) || '[]'))
  const units = ref<SavedUnit[]>(JSON.parse(localStorage.getItem(STORAGE_UNITS_KEY) || '[]'))

  function addBuilding(b: SavedBuilding) {
    buildings.value.unshift(b)
    localStorage.setItem(STORAGE_BUILDINGS_KEY, JSON.stringify(buildings.value))
  }

  function addUnit(u: SavedUnit) {
    units.value.unshift(u)
    localStorage.setItem(STORAGE_UNITS_KEY, JSON.stringify(units.value))
  }

  return { buildings, units, addBuilding, addUnit }
})
