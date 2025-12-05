# Lazy-Loaded Material-UI Components

This directory contains lazy-loaded wrappers for heavy Material-UI components to optimize bundle size and improve initial load performance.

## Available Components

### LazyDataGrid

A lazy-loaded wrapper for `@mui/x-data-grid`'s DataGrid component.

**Usage:**

```tsx
import LazyDataGrid from '@/components/LazyDataGrid';

function MyPage() {
  const rows = [...];
  const columns = [...];

  return (
    <LazyDataGrid
      rows={rows}
      columns={columns}
      pageSize={10}
      rowsPerPageOptions={[5, 10, 20]}
      checkboxSelection
      disableSelectionOnClick
    />
  );
}
```

**Benefits:**
- DataGrid library (~200KB) is only loaded when the component is rendered
- Automatic loading indicator while the chunk is being fetched
- Error handling with retry logic for failed chunk loads

### LazyDatePicker

A lazy-loaded wrapper for `@mui/x-date-pickers`'s DatePicker component.

**Usage:**

```tsx
import LazyDatePicker from '@/components/LazyDatePicker';
import { LocalizationProvider } from '@mui/x-date-pickers';
import { AdapterDateFns } from '@mui/x-date-pickers/AdapterDateFns';

function MyForm() {
  const [date, setDate] = useState<Date | null>(null);

  return (
    <LocalizationProvider dateAdapter={AdapterDateFns}>
      <LazyDatePicker
        label="Select Date"
        value={date}
        onChange={(newValue) => setDate(newValue)}
      />
    </LocalizationProvider>
  );
}
```

**Benefits:**
- DatePicker library is only loaded when the component is rendered
- Automatic loading indicator while the chunk is being fetched
- Error handling with retry logic for failed chunk loads

## Implementation Details

Both components use:
- `React.lazy()` for dynamic imports
- `LazyComponentWrapper` for Suspense boundaries and error handling
- Automatic retry logic (up to 3 attempts with exponential backoff)
- User-friendly error messages with manual retry options

## When to Use

Use these lazy-loaded wrappers instead of direct imports when:
- The component is not needed on initial page load
- The component is used in a specific route or feature
- You want to reduce the initial bundle size

## Performance Impact

Using lazy-loaded components:
- Reduces initial bundle size by ~200-300KB (depending on which components are used)
- Improves Time to Interactive (TTI) metrics
- Enables better code splitting and caching strategies
- Slightly delays the first render of the component (typically <500ms on good connections)

## Pages Using Lazy Components

The following pages are configured to use lazy-loaded Material-UI components:

- **MembersPage**: LazyDataGrid for member listing
- **LoansPage**: LazyDataGrid for loan listing, LazyDatePicker for date filters
- **SavingsPage**: LazyDataGrid for savings listing
- **PaymentsPage**: LazyDataGrid for payment listing, LazyDatePicker for date filters

See the individual page files for implementation examples.
