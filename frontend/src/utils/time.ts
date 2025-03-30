/**
 * 格式化时间戳为可读字符串
 * @param timestamp 时间戳（可以是数字或日期对象）
 * @returns 格式化后的时间字符串
 */
export function formatTime(timestamp: number | Date | undefined | null): string {
  if (!timestamp) {
    return '-';
  }

  const date = new Date(timestamp);
  
  if (isNaN(date.getTime())) {
    return '-';
  }

  const year = date.getFullYear();
  const month = String(date.getMonth() + 1).padStart(2, '0');
  const day = String(date.getDate()).padStart(2, '0');
  const hours = String(date.getHours()).padStart(2, '0');
  const minutes = String(date.getMinutes()).padStart(2, '0');
  const seconds = String(date.getSeconds()).padStart(2, '0');

  return `${year}-${month}-${day} ${hours}:${minutes}:${seconds}`;
} 