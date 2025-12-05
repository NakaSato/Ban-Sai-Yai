import React, { useEffect, useState } from 'react';
import { Card, CardContent, Typography, Box, CircularProgress, Alert } from '@mui/material';
import { BarChart, Bar, PieChart, Pie, Cell, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import axios from 'axios';

interface FinancialPreviewsData {
  incomeData: {
    labels: string[];
    datasets: {
      income: number;
      expenses: number;
    };
  };
  balanceSheetData: {
    labels: string[];
    datasets: {
      cashAndBank: number;
      loansReceivable: number;
      otherAssets: number;
    };
  };
}

const COLORS = ['#0088FE', '#00C49F', '#FFBB28', '#FF8042'];

const FinancialStatementPreviews: React.FC = () => {
  const [data, setData] = useState<FinancialPreviewsData | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    fetchFinancialPreviews();
  }, []);

  const fetchFinancialPreviews = async () => {
    try {
      setLoading(true);
      setError(null);
      const response = await axios.get('/api/dashboard/secretary/financial-previews');
      setData(response.data);
    } catch (err: any) {
      console.error('Error fetching financial previews:', err);
      setError(err.response?.data?.message || 'Failed to load financial previews');
    } finally {
      setLoading(false);
    }
  };

  if (loading) {
    return (
      <Card>
        <CardContent>
          <Box display="flex" justifyContent="center" alignItems="center" minHeight={400}>
            <CircularProgress />
          </Box>
        </CardContent>
      </Card>
    );
  }

  if (error) {
    return (
      <Card>
        <CardContent>
          <Alert severity="error">{error}</Alert>
        </CardContent>
      </Card>
    );
  }

  if (!data) {
    return (
      <Card>
        <CardContent>
          <Alert severity="info">No financial data available</Alert>
        </CardContent>
      </Card>
    );
  }

  // Prepare data for income vs expenses bar chart
  const incomeExpensesData = [
    {
      name: 'Income',
      amount: data.incomeData.datasets.income || 0,
    },
    {
      name: 'Expenses',
      amount: data.incomeData.datasets.expenses || 0,
    },
  ];

  // Prepare data for asset distribution pie chart
  const assetDistributionData = [
    {
      name: 'Cash & Bank',
      value: data.balanceSheetData.datasets.cashAndBank || 0,
    },
    {
      name: 'Loans Receivable',
      value: data.balanceSheetData.datasets.loansReceivable || 0,
    },
    {
      name: 'Other Assets',
      value: data.balanceSheetData.datasets.otherAssets || 0,
    },
  ].filter(item => item.value > 0); // Only show non-zero values

  return (
    <Box>
      <Typography variant="h5" gutterBottom sx={{ mb: 3 }}>
        Financial Statement Previews
      </Typography>

      {/* Income vs Expenses Bar Chart */}
      <Card sx={{ mb: 3 }}>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Income vs Expenses
          </Typography>
          <ResponsiveContainer width="100%" height={300}>
            <BarChart data={incomeExpensesData}>
              <CartesianGrid strokeDasharray="3 3" />
              <XAxis dataKey="name" />
              <YAxis />
              <Tooltip 
                formatter={(value: number) => `฿${value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
              />
              <Legend />
              <Bar dataKey="amount" fill="#8884d8" name="Amount (฿)" />
            </BarChart>
          </ResponsiveContainer>
        </CardContent>
      </Card>

      {/* Asset Distribution Pie Chart */}
      <Card>
        <CardContent>
          <Typography variant="h6" gutterBottom>
            Asset Distribution
          </Typography>
          {assetDistributionData.length > 0 ? (
            <ResponsiveContainer width="100%" height={300}>
              <PieChart>
                <Pie
                  data={assetDistributionData}
                  cx="50%"
                  cy="50%"
                  labelLine={false}
                  label={({ name, percent }) => `${name}: ${(percent * 100).toFixed(0)}%`}
                  outerRadius={80}
                  fill="#8884d8"
                  dataKey="value"
                >
                  {assetDistributionData.map((entry, index) => (
                    <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
                  ))}
                </Pie>
                <Tooltip 
                  formatter={(value: number) => `฿${value.toLocaleString('en-US', { minimumFractionDigits: 2, maximumFractionDigits: 2 })}`}
                />
                <Legend />
              </PieChart>
            </ResponsiveContainer>
          ) : (
            <Alert severity="info">No asset data available for the current period</Alert>
          )}
        </CardContent>
      </Card>
    </Box>
  );
};

export default FinancialStatementPreviews;
