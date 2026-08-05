const imagePromises = new Map<string, Promise<string>>()

export function preloadImage(url?: string) {
  if (!url || url.startsWith('data:')) return Promise.resolve(url ?? '')
  const cached = imagePromises.get(url)
  if (cached) return cached
  const promise = new Promise<string>(resolve => {
    const image = new Image()
    image.decoding = 'async'
    image.onload = () => resolve(url)
    image.onerror = () => resolve(url)
    image.src = url
  })
  imagePromises.set(url, promise)
  return promise
}

export function preloadImages(urls: Array<string | undefined>, limit = 8) {
  return Promise.all([...new Set(urls.filter(Boolean) as string[])].slice(0, limit).map(preloadImage))
}
