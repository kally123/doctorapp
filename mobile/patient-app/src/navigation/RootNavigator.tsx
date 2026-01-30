import React from 'react';
import { createNativeStackNavigator } from '@react-navigation/native-stack';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { Ionicons } from '@expo/vector-icons';

import { useAuth } from '../contexts/AuthContext';

// Auth Screens
import LoginScreen from '../screens/auth/LoginScreen';
import RegisterScreen from '../screens/auth/RegisterScreen';
import ForgotPasswordScreen from '../screens/auth/ForgotPasswordScreen';
import OTPVerificationScreen from '../screens/auth/OTPVerificationScreen';

// Main Screens
import HomeScreen from '../screens/home/HomeScreen';
import DoctorSearchScreen from '../screens/doctors/DoctorSearchScreen';
import DoctorProfileScreen from '../screens/doctors/DoctorProfileScreen';
import BookAppointmentScreen from '../screens/appointments/BookAppointmentScreen';
import AppointmentsScreen from '../screens/appointments/AppointmentsScreen';
import AppointmentDetailScreen from '../screens/appointments/AppointmentDetailScreen';
import VideoCallScreen from '../screens/consultation/VideoCallScreen';
import ChatScreen from '../screens/consultation/ChatScreen';
import ProfileScreen from '../screens/profile/ProfileScreen';
import HealthRecordsScreen from '../screens/health/HealthRecordsScreen';
import PrescriptionsScreen from '../screens/prescriptions/PrescriptionsScreen';
import PharmacyScreen from '../screens/pharmacy/PharmacyScreen';
import ArticlesScreen from '../screens/articles/ArticlesScreen';
import ArticleDetailScreen from '../screens/articles/ArticleDetailScreen';
import NotificationsScreen from '../screens/notifications/NotificationsScreen';
import SettingsScreen from '../screens/settings/SettingsScreen';

export type RootStackParamList = {
  Auth: undefined;
  Main: undefined;
  DoctorProfile: { doctorId: string };
  BookAppointment: { doctorId: string; slotId?: string };
  AppointmentDetail: { appointmentId: string };
  VideoCall: { consultationId: string };
  Chat: { consultationId: string };
  ArticleDetail: { articleId: string };
  Notifications: undefined;
  Settings: undefined;
};

export type AuthStackParamList = {
  Login: undefined;
  Register: undefined;
  ForgotPassword: undefined;
  OTPVerification: { phone: string; type: 'login' | 'register' };
};

export type MainTabParamList = {
  Home: undefined;
  Doctors: undefined;
  Appointments: undefined;
  Health: undefined;
  Profile: undefined;
};

const RootStack = createNativeStackNavigator<RootStackParamList>();
const AuthStack = createNativeStackNavigator<AuthStackParamList>();
const MainTab = createBottomTabNavigator<MainTabParamList>();

function AuthNavigator() {
  return (
    <AuthStack.Navigator
      screenOptions={{
        headerShown: false,
      }}
    >
      <AuthStack.Screen name="Login" component={LoginScreen} />
      <AuthStack.Screen name="Register" component={RegisterScreen} />
      <AuthStack.Screen name="ForgotPassword" component={ForgotPasswordScreen} />
      <AuthStack.Screen name="OTPVerification" component={OTPVerificationScreen} />
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
            case 'Home':
              iconName = focused ? 'home' : 'home-outline';
              break;
            case 'Doctors':
              iconName = focused ? 'search' : 'search-outline';
              break;
            case 'Appointments':
              iconName = focused ? 'calendar' : 'calendar-outline';
              break;
            case 'Health':
              iconName = focused ? 'heart' : 'heart-outline';
              break;
            case 'Profile':
              iconName = focused ? 'person' : 'person-outline';
              break;
            default:
              iconName = 'home-outline';
          }

          return <Ionicons name={iconName} size={size} color={color} />;
        },
        tabBarActiveTintColor: '#2563eb',
        tabBarInactiveTintColor: 'gray',
        tabBarStyle: {
          paddingBottom: 5,
          paddingTop: 5,
          height: 60,
        },
      })}
    >
      <MainTab.Screen name="Home" component={HomeScreen} />
      <MainTab.Screen name="Doctors" component={DoctorSearchScreen} />
      <MainTab.Screen name="Appointments" component={AppointmentsScreen} />
      <MainTab.Screen name="Health" component={HealthRecordsScreen} />
      <MainTab.Screen name="Profile" component={ProfileScreen} />
    </MainTab.Navigator>
  );
}

export default function RootNavigator() {
  const { isAuthenticated, isLoading } = useAuth();

  if (isLoading) {
    // TODO: Show splash screen
    return null;
  }

  return (
    <RootStack.Navigator
      screenOptions={{
        headerShown: false,
      }}
    >
      {isAuthenticated ? (
        <>
          <RootStack.Screen name="Main" component={MainNavigator} />
          <RootStack.Screen name="DoctorProfile" component={DoctorProfileScreen} />
          <RootStack.Screen name="BookAppointment" component={BookAppointmentScreen} />
          <RootStack.Screen name="AppointmentDetail" component={AppointmentDetailScreen} />
          <RootStack.Screen
            name="VideoCall"
            component={VideoCallScreen}
            options={{ orientation: 'all' }}
          />
          <RootStack.Screen name="Chat" component={ChatScreen} />
          <RootStack.Screen name="ArticleDetail" component={ArticleDetailScreen} />
          <RootStack.Screen name="Notifications" component={NotificationsScreen} />
          <RootStack.Screen name="Settings" component={SettingsScreen} />
        </>
      ) : (
        <RootStack.Screen name="Auth" component={AuthNavigator} />
      )}
    </RootStack.Navigator>
  );
}
