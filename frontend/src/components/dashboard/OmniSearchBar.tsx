import React, { useState, useEffect, useCallback, useRef } from 'react';
import {
  TextField,
  Autocomplete,
  Avatar,
  Box,
  Typography,
  CircularProgress,
  InputAdornment,
} from '@mui/material';
import { Search as SearchIcon } from '@mui/icons-material';
import { useNavigate } from 'react-router-dom';
import { useSearchMembersQuery } from '@/store/api/dashboardApi';
import { MemberSearchResult } from '@/types';

const OmniSearchBar: React.FC = () => {
  const [searchQuery, setSearchQuery] = useState('');
  const [debouncedQuery, setDebouncedQuery] = useState('');
  const [open, setOpen] = useState(false);
  const navigate = useNavigate();
  const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null);

  // Debounce search query (300ms)
  useEffect(() => {
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current);
    }

    debounceTimerRef.current = setTimeout(() => {
      setDebouncedQuery(searchQuery);
    }, 300);

    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current);
      }
    };
  }, [searchQuery]);

  // Only fetch when we have a debounced query
  const { data: searchResults = [], isLoading } = useSearchMembersQuery(
    { q: debouncedQuery, limit: 5 },
    { skip: !debouncedQuery || debouncedQuery.length < 2 }
  );

  const handleInputChange = useCallback((event: React.SyntheticEvent, value: string) => {
    setSearchQuery(value);
    setOpen(value.length >= 2);
  }, []);

  const handleSelect = useCallback(
    (event: React.SyntheticEvent, value: MemberSearchResult | null) => {
      if (value) {
        navigate(`/members/${value.memberId}`);
        setSearchQuery('');
        setOpen(false);
      }
    },
    [navigate]
  );

  return (
    <Autocomplete
      freeSolo
      open={open}
      onOpen={() => setOpen(searchQuery.length >= 2)}
      onClose={() => setOpen(false)}
      options={searchResults}
      loading={isLoading}
      getOptionLabel={(option) => {
        if (typeof option === 'string') return option;
        return `${option.firstName} ${option.lastName}`;
      }}
      filterOptions={(x) => x} // Disable client-side filtering since we filter on server
      onInputChange={handleInputChange}
      onChange={handleSelect}
      renderInput={(params) => (
        <TextField
          {...params}
          placeholder="Search members by name, ID, or national ID..."
          variant="outlined"
          size="small"
          sx={{ minWidth: 300 }}
          InputProps={{
            ...params.InputProps,
            startAdornment: (
              <InputAdornment position="start">
                <SearchIcon />
              </InputAdornment>
            ),
            endAdornment: (
              <>
                {isLoading ? <CircularProgress color="inherit" size={20} /> : null}
                {params.InputProps.endAdornment}
              </>
            ),
          }}
        />
      )}
      renderOption={(props, option) => (
        <Box component="li" {...props} sx={{ display: 'flex', alignItems: 'center', gap: 2 }}>
          <Avatar
            src={option.thumbnailUrl}
            alt={`${option.firstName} ${option.lastName}`}
            sx={{ width: 32, height: 32 }}
          />
          <Box>
            <Typography variant="body2">
              {option.firstName} {option.lastName}
            </Typography>
            <Typography variant="caption" color="text.secondary">
              ID: {option.memberId} • {option.status}
            </Typography>
          </Box>
        </Box>
      )}
      noOptionsText={
        searchQuery.length < 2
          ? 'Type at least 2 characters to search'
          : 'No members found'
      }
    />
  );
};

export default OmniSearchBar;
