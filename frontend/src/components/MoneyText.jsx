const krwFormatter = new Intl.NumberFormat('ko-KR', {
  style: 'currency',
  currency: 'KRW',
  maximumFractionDigits: 0
});

export default function MoneyText({ amount, className }) {
  const value = Number(amount ?? 0);

  return <span className={className}>{krwFormatter.format(Number.isFinite(value) ? value : 0)}</span>;
}
