import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';

import { useAuth } from '../contexts/AuthContext';

// Auth Screens
import LoginScreen from '../screens/auth/LoginScreen';
import RegisterScreen from '../screens/auth/RegisterScreen';

// Main Screens
import DashboardScreen from '../screens/dashboard/DashboardScreen';
import ScheduleScreen from '../screens/schedule/ScheduleScreen';
import AppointmentDetailScreen from '../screens/schedule/AppointmentDetailScreen';
import PatientsScreen from '../screens/patients/PatientsScreen';
import PatientDetailScreen from '../screens/patients/PatientDetailScreen';
import ConsultationScreen from '../screens/consultation/ConsultationScreen';
import VideoCallScreen from '../screens/consultation/VideoCallScreen';
import ChatScreen from '../screens/consultation/ChatScreen';
import PrescriptionScreen from '../screens/prescription/PrescriptionScreen';
import EarningsScreen from '../screens/earnings/EarningsScreen';
import ProfileScreen from '../screens/profile/ProfileScreen';
import SettingsScreen from '../screens/settings/SettingsScreen';
import ReviewsScreen from '../screens/reviews/ReviewsScreen';

export type RootStackParamList = {
  Auth: undefined;
  Main: undefined;
  AppointmentDetail: { appointmentId: string };
  PatientDetail: { patientId: string };
  Consultation: { consultationId: string };
  VideoCall: { consultationId: string };
  Chat: { consultationId: string };
  Prescription: { consultationId: string; patientId: string };
  Settings: undefined;
  Reviews: undefined;
};

export type AuthStackParamList = {
  Login: undefined;
  Register: undefined;
};

export type MainTabParamList = {
  Dashboard: undefined;
  Schedule: undefined;
  Patients: undefined;
  Earnings: undefined;
  Profile: undefined;
};

const RootStack = createNativeStackNavigator<RootStackParamList>();
const AuthStack = createNativeStackNavigator<AuthStackParamList>();
const MainTab = createBottomTabNavigator<MainTabParamList>();

function AuthNavigator() {
  return (
    <AuthStack.Navigator screenOptions={{ headerShown: false }}>
      <AuthStack.Screen name="Login" component={LoginScreen} />
      <AuthStack.Screen name="Register" component={RegisterScreen} />
    </AuthStack.Navigator>
  );
}

function MainNavigator() {
  return (
    <MainTab.Navigator
      screenOptions={({ route }) => ({
        headerShown: false,
        tabBarIcon: ({ focused, color, size }) => {
          let iconName: keyof typeof Ionicons.glyphMap;

          switch (route.name) {
            case 'Dashboard':
              iconName = focused ? 'grid' : 'grid-outline';
              break;
            case 'Schedule':
              iconName = focused ? 'calendar' : 'calendar-outline';
              break;
            case 'Patients':
              iconName = focused ? 'people' : 'people-outline';
              break;
            case 'Earnings':
              iconName = focused ? 'wallet' : 'wallet-outline';
              break;
            case 'Profile':
              iconName = focused ? 'person' : 'person-outline';
              break;
            default:
              iconName = 'grid-outline';
          }

          return <Ionicons name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: '#059669',
        tabBarInactiveTintColor: 'gray',
        tabBarStyle: {
          paddingBottom: 5,
          paddingTop: 5,
          height: 60,
        },
      })}
    >
      <MainTab.Screen name="Dashboard" component={DashboardScreen} />
      <MainTab.Screen name="Schedule" component={ScheduleScreen} />
      <MainTab.Screen name="Patients" component={PatientsScreen} />
      <MainTab.Screen name="Earnings" component={EarningsScreen} />
      <MainTab.Screen name="Profile" component={ProfileScreen} />
    </MainTab.Navigator>
  );
}

export default function RootNavigator() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    return null;
  }

  return (
    <RootStack.Navigator screenOptions={{ headerShown: false }}>
      {isAuthenticated ? (
        <>
          <RootStack.Screen name="Main" component={MainNavigator} />
          <RootStack.Screen name="AppointmentDetail" component={AppointmentDetailScreen} />
          <RootStack.Screen name="PatientDetail" component={PatientDetailScreen} />
          <RootStack.Screen name="Consultation" component={ConsultationScreen} />
          <RootStack.Screen
            name="VideoCall"
            component={VideoCallScreen}
            options={{ orientation: 'all' }}
          />
          <RootStack.Screen name="Chat" component={ChatScreen} />
          <RootStack.Screen name="Prescription" component={PrescriptionScreen} />
          <RootStack.Screen name="Settings" component={SettingsScreen} />
          <RootStack.Screen name="Reviews" component={ReviewsScreen} />
        </>
      ) : (
        <RootStack.Screen name="Auth" component={AuthNavigator} />
      )}
    </RootStack.Navigator>
  );
}
