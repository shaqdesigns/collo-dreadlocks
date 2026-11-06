export function StatusMessage({
  error,
  loading,
}: {
  error?: string | null
  loading?: boolean
}) {
  if (loading) {
    return <p className="text-sm text-muted">Loading the catalogue…</p>
  }
  if (error) {
    return (
      <p className="border border-clay/30 bg-clay/10 px-4 py-3 text-sm text-clay">
        {error}
      </p>
    )
  }
  return null
}
