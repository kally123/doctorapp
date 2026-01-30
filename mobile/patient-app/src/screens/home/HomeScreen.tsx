import React from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  Image,
  TextInput,
  FlatList,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { useAuth } from '../../contexts/AuthContext';
import { useNotifications } from '../../contexts/NotificationContext';

const SPECIALTIES = [
  { id: '1', name: 'General', icon: 'medkit-outline', color: '#3b82f6' },
  { id: '2', name: 'Cardiology', icon: 'heart-outline', color: '#ef4444' },
  { id: '3', name: 'Dermatology', icon: 'body-outline', color: '#f59e0b' },
  { id: '4', name: 'Orthopedics', icon: 'fitness-outline', color: '#10b981' },
  { id: '5', name: 'Pediatrics', icon: 'happy-outline', color: '#8b5cf6' },
  { id: '6', name: 'Neurology', icon: 'pulse-outline', color: '#ec4899' },
  { id: '7', name: 'Psychiatry', icon: 'chatbubbles-outline', color: '#14b8a6' },
  { id: '8', name: 'More', icon: 'grid-outline', color: '#6b7280' },
];

const UPCOMING_APPOINTMENTS = [
  {
    id: '1',
    doctorName: 'Dr. Sarah Smith',
    specialty: 'Cardiology',
    date: 'Today',
    time: '10:30 AM',
    type: 'Video',
    avatar: null,
  },
  {
    id: '2',
    doctorName: 'Dr. Michael Chen',
    specialty: 'General Medicine',
    date: 'Tomorrow',
    time: '2:00 PM',
    type: 'In-Person',
    avatar: null,
  },
];

export default function HomeScreen() {
  const navigation = useNavigation();
  const { user } = useAuth();
  const { unreadCount } = useNotifications();

  const renderSpecialtyItem = ({ item }: { item: typeof SPECIALTIES[0] }) => (
    <TouchableOpacity style={styles.specialtyItem} onPress={() => {}}>
      <View style={[styles.specialtyIcon, { backgroundColor: `${item.color}20` }]}>
        <Ionicons name={item.icon as any} size={24} color={item.color} />
      </View>
      <Text style={styles.specialtyName}>{item.name}</Text>
    </TouchableOpacity>
  );

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView showsVerticalScrollIndicator={false}>
        {/* Header */}
        <LinearGradient colors={['#2563eb', '#1d4ed8']} style={styles.header}>
          <View style={styles.headerTop}>
            <View>
              <Text style={styles.greeting}>Hello, {user?.firstName || 'there'}! 👋</Text>
              <Text style={styles.subtitle}>How are you feeling today?</Text>
            </View>
            <TouchableOpacity
              style={styles.notificationButton}
              onPress={() => navigation.navigate('Notifications' as never)}
            >
              <Ionicons name="notifications-outline" size={24} color="white" />
              {unreadCount > 0 && (
                <View style={styles.badge}>
                  <Text style={styles.badgeText}>{unreadCount > 9 ? '9+' : unreadCount}</Text>
                </View>
              )}
            </TouchableOpacity>
          </View>

          {/* Search Bar */}
          <TouchableOpacity
            style={styles.searchBar}
            onPress={() => navigation.navigate('Doctors' as never)}
          >
            <Ionicons name="search-outline" size={20} color="#9ca3af" />
            <Text style={styles.searchPlaceholder}>Search doctors, specialties...</Text>
          </TouchableOpacity>
        </LinearGradient>

        {/* Quick Actions */}
        <View style={styles.quickActions}>
          <TouchableOpacity style={styles.quickAction}>
            <View style={[styles.quickActionIcon, { backgroundColor: '#dbeafe' }]}>
              <Ionicons name="videocam-outline" size={24} color="#2563eb" />
            </View>
            <Text style={styles.quickActionText}>Video Consult</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.quickAction}>
            <View style={[styles.quickActionIcon, { backgroundColor: '#dcfce7' }]}>
              <Ionicons name="location-outline" size={24} color="#16a34a" />
            </View>
            <Text style={styles.quickActionText}>Find Clinic</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.quickAction}>
            <View style={[styles.quickActionIcon, { backgroundColor: '#fef3c7' }]}>
              <Ionicons name="medical-outline" size={24} color="#d97706" />
            </View>
            <Text style={styles.quickActionText}>Pharmacy</Text>
          </TouchableOpacity>
          <TouchableOpacity style={styles.quickAction}>
            <View style={[styles.quickActionIcon, { backgroundColor: '#fce7f3' }]}>
              <Ionicons name="flask-outline" size={24} color="#db2777" />
            </View>
            <Text style={styles.quickActionText}>Lab Tests</Text>
          </TouchableOpacity>
        </View>

        {/* Specialties */}
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Specialties</Text>
            <TouchableOpacity>
              <Text style={styles.seeAll}>See All</Text>
            </TouchableOpacity>
          </View>
          <FlatList
            data={SPECIALTIES}
            renderItem={renderSpecialtyItem}
            keyExtractor={(item) => item.id}
            horizontal
            showsHorizontalScrollIndicator={false}
            contentContainerStyle={styles.specialtiesList}
          />
        </View>

        {/* Upcoming Appointments */}
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Upcoming Appointments</Text>
            <TouchableOpacity onPress={() => navigation.navigate('Appointments' as never)}>
              <Text style={styles.seeAll}>See All</Text>
            </TouchableOpacity>
          </View>

          {UPCOMING_APPOINTMENTS.length > 0 ? (
            UPCOMING_APPOINTMENTS.map((appointment) => (
              <TouchableOpacity key={appointment.id} style={styles.appointmentCard}>
                <View style={styles.appointmentLeft}>
                  <View style={styles.doctorAvatar}>
                    <Ionicons name="person" size={24} color="#9ca3af" />
                  </View>
                  <View>
                    <Text style={styles.doctorName}>{appointment.doctorName}</Text>
                    <Text style={styles.appointmentSpecialty}>{appointment.specialty}</Text>
                  </View>
                </View>
                <View style={styles.appointmentRight}>
                  <Text style={styles.appointmentDate}>{appointment.date}</Text>
                  <Text style={styles.appointmentTime}>{appointment.time}</Text>
                  <View
                    style={[
                      styles.appointmentType,
                      { backgroundColor: appointment.type === 'Video' ? '#dbeafe' : '#dcfce7' },
                    ]}
                  >
                    <Ionicons
                      name={appointment.type === 'Video' ? 'videocam' : 'location'}
                      size={12}
                      color={appointment.type === 'Video' ? '#2563eb' : '#16a34a'}
                    />
                    <Text
                      style={[
                        styles.appointmentTypeText,
                        { color: appointment.type === 'Video' ? '#2563eb' : '#16a34a' },
                      ]}
                    >
                      {appointment.type}
                    </Text>
                  </View>
                </View>
              </TouchableOpacity>
            ))
          ) : (
            <View style={styles.emptyState}>
              <Ionicons name="calendar-outline" size={48} color="#d1d5db" />
              <Text style={styles.emptyStateText}>No upcoming appointments</Text>
              <TouchableOpacity style={styles.bookButton}>
                <Text style={styles.bookButtonText}>Book Now</Text>
              </TouchableOpacity>
            </View>
          )}
        </View>

        {/* Health Tips Banner */}
        <TouchableOpacity style={styles.banner}>
          <LinearGradient
            colors={['#10b981', '#059669']}
            style={styles.bannerGradient}
            start={{ x: 0, y: 0 }}
            end={{ x: 1, y: 0 }}
          >
            <View style={styles.bannerContent}>
              <Text style={styles.bannerTitle}>Health Tips</Text>
              <Text style={styles.bannerSubtitle}>
                Read our latest articles on staying healthy
              </Text>
            </View>
            <Ionicons name="newspaper-outline" size={48} color="rgba(255,255,255,0.3)" />
          </LinearGradient>
        </TouchableOpacity>
      </ScrollView>
    </SafeAreaView>
  );
}

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f3f4f6',
  },
  header: {
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 30,
    borderBottomLeftRadius: 24,
    borderBottomRightRadius: 24,
  },
  headerTop: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 20,
  },
  greeting: {
    fontSize: 24,
    fontWeight: 'bold',
    color: 'white',
  },
  subtitle: {
    fontSize: 14,
    color: 'rgba(255,255,255,0.8)',
    marginTop: 4,
  },
  notificationButton: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: 'rgba(255,255,255,0.2)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  badge: {
    position: 'absolute',
    top: -2,
    right: -2,
    backgroundColor: '#ef4444',
    borderRadius: 10,
    minWidth: 20,
    height: 20,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 4,
  },
  badgeText: {
    color: 'white',
    fontSize: 12,
    fontWeight: 'bold',
  },
  searchBar: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: 'white',
    borderRadius: 12,
    paddingHorizontal: 16,
    paddingVertical: 12,
  },
  searchPlaceholder: {
    marginLeft: 10,
    color: '#9ca3af',
    fontSize: 16,
  },
  quickActions: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    paddingVertical: 20,
    marginTop: -20,
    backgroundColor: 'white',
    marginHorizontal: 20,
    borderRadius: 16,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 8,
    elevation: 4,
  },
  quickAction: {
    alignItems: 'center',
  },
  quickActionIcon: {
    width: 48,
    height: 48,
    borderRadius: 12,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 8,
  },
  quickActionText: {
    fontSize: 12,
    color: '#374151',
  },
  section: {
    paddingHorizontal: 20,
    paddingTop: 24,
  },
  sectionHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 16,
  },
  sectionTitle: {
    fontSize: 18,
    fontWeight: '600',
    color: '#111827',
  },
  seeAll: {
    fontSize: 14,
    color: '#2563eb',
  },
  specialtiesList: {
    paddingRight: 20,
  },
  specialtyItem: {
    alignItems: 'center',
    marginRight: 16,
    width: 72,
  },
  specialtyIcon: {
    width: 56,
    height: 56,
    borderRadius: 16,
    alignItems: 'center',
    justifyContent: 'center',
    marginBottom: 8,
  },
  specialtyName: {
    fontSize: 12,
    color: '#374151',
    textAlign: 'center',
  },
  appointmentCard: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    backgroundColor: 'white',
    borderRadius: 16,
    padding: 16,
    marginBottom: 12,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.05,
    shadowRadius: 4,
    elevation: 2,
  },
  appointmentLeft: {
    flexDirection: 'row',
    alignItems: 'center',
  },
  doctorAvatar: {
    width: 48,
    height: 48,
    borderRadius: 24,
    backgroundColor: '#f3f4f6',
    alignItems: 'center',
    justifyContent: 'center',
    marginRight: 12,
  },
  doctorName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  appointmentSpecialty: {
    fontSize: 14,
    color: '#6b7280',
    marginTop: 2,
  },
  appointmentRight: {
    alignItems: 'flex-end',
  },
  appointmentDate: {
    fontSize: 14,
    fontWeight: '500',
    color: '#111827',
  },
  appointmentTime: {
    fontSize: 12,
    color: '#6b7280',
    marginTop: 2,
  },
  appointmentType: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 8,
    marginTop: 8,
  },
  appointmentTypeText: {
    fontSize: 12,
    marginLeft: 4,
    fontWeight: '500',
  },
  emptyState: {
    alignItems: 'center',
    paddingVertical: 32,
    backgroundColor: 'white',
    borderRadius: 16,
  },
  emptyStateText: {
    fontSize: 14,
    color: '#6b7280',
    marginTop: 12,
  },
  bookButton: {
    backgroundColor: '#2563eb',
    paddingHorizontal: 24,
    paddingVertical: 10,
    borderRadius: 8,
    marginTop: 16,
  },
  bookButtonText: {
    color: 'white',
    fontWeight: '600',
  },
  banner: {
    marginHorizontal: 20,
    marginVertical: 24,
    borderRadius: 16,
    overflow: 'hidden',
  },
  bannerGradient: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 20,
  },
  bannerContent: {
    flex: 1,
  },
  bannerTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    color: 'white',
  },
  bannerSubtitle: {
    fontSize: 14,
    color: 'rgba(255,255,255,0.8)',
    marginTop: 4,
  },
});
