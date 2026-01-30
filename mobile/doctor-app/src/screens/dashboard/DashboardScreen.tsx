import React from 'react';
import {
  View,
  Text,
  ScrollView,
  TouchableOpacity,
  StyleSheet,
  Dimensions,
} from 'react-native';
import { SafeAreaView } from 'react-native-safe-area-context';
import { LinearGradient } from 'expo-linear-gradient';
import { Ionicons } from '@expo/vector-icons';
import { useNavigation } from '@react-navigation/native';
import { LineChart } from 'react-native-chart-kit';
import { useAuth } from '../../contexts/AuthContext';

const screenWidth = Dimensions.get('window').width;

const TODAYS_APPOINTMENTS = [
  {
    id: '1',
    patientName: 'John Smith',
    time: '9:00 AM',
    type: 'Video',
    status: 'upcoming',
    reason: 'Follow-up consultation',
  },
  {
    id: '2',
    patientName: 'Sarah Johnson',
    time: '10:30 AM',
    type: 'Video',
    status: 'in-progress',
    reason: 'Chest pain evaluation',
  },
  {
    id: '3',
    patientName: 'Mike Brown',
    time: '2:00 PM',
    type: 'In-Person',
    status: 'upcoming',
    reason: 'Annual checkup',
  },
];

const STATS = {
  todayAppointments: 8,
  completedToday: 3,
  pendingReviews: 5,
  totalPatients: 234,
  monthlyEarnings: 12500,
  avgRating: 4.8,
};

export default function DashboardScreen() {
  const navigation = useNavigation();
  const { user } = useAuth();

  const chartData = {
    labels: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'],
    datasets: [
      {
        data: [5, 8, 6, 9, 7, 4, 6],
        color: (opacity = 1) => `rgba(5, 150, 105, ${opacity})`,
        strokeWidth: 2,
      },
    ],
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'in-progress':
        return '#22c55e';
      case 'upcoming':
        return '#3b82f6';
      case 'completed':
        return '#6b7280';
      default:
        return '#6b7280';
    }
  };

  return (
    <SafeAreaView style={styles.container}>
      <ScrollView showsVerticalScrollIndicator={false}>
        {/* Header */}
        <LinearGradient colors={['#059669', '#047857']} style={styles.header}>
          <View style={styles.headerContent}>
            <View>
              <Text style={styles.greeting}>Good morning,</Text>
              <Text style={styles.doctorName}>Dr. {user?.lastName || 'Doctor'}</Text>
            </View>
            <TouchableOpacity style={styles.notificationBtn}>
              <Ionicons name="notifications-outline" size={24} color="white" />
              <View style={styles.notificationBadge}>
                <Text style={styles.badgeText}>3</Text>
              </View>
            </TouchableOpacity>
          </View>

          {/* Quick Stats */}
          <View style={styles.quickStats}>
            <View style={styles.quickStatItem}>
              <Text style={styles.quickStatValue}>{STATS.todayAppointments}</Text>
              <Text style={styles.quickStatLabel}>Today's Appts</Text>
            </View>
            <View style={styles.quickStatDivider} />
            <View style={styles.quickStatItem}>
              <Text style={styles.quickStatValue}>{STATS.pendingReviews}</Text>
              <Text style={styles.quickStatLabel}>Pending Reviews</Text>
            </View>
            <View style={styles.quickStatDivider} />
            <View style={styles.quickStatItem}>
              <Text style={styles.quickStatValue}>⭐ {STATS.avgRating}</Text>
              <Text style={styles.quickStatLabel}>Rating</Text>
            </View>
          </View>
        </LinearGradient>

        {/* Stats Cards */}
        <View style={styles.statsContainer}>
          <View style={styles.statsRow}>
            <View style={[styles.statCard, { backgroundColor: '#dbeafe' }]}>
              <Ionicons name="people" size={24} color="#2563eb" />
              <Text style={styles.statValue}>{STATS.totalPatients}</Text>
              <Text style={styles.statLabel}>Total Patients</Text>
            </View>
            <View style={[styles.statCard, { backgroundColor: '#dcfce7' }]}>
              <Ionicons name="wallet" size={24} color="#16a34a" />
              <Text style={styles.statValue}>₹{STATS.monthlyEarnings.toLocaleString()}</Text>
              <Text style={styles.statLabel}>This Month</Text>
            </View>
          </View>
        </View>

        {/* Today's Schedule */}
        <View style={styles.section}>
          <View style={styles.sectionHeader}>
            <Text style={styles.sectionTitle}>Today's Schedule</Text>
            <TouchableOpacity onPress={() => navigation.navigate('Schedule' as never)}>
              <Text style={styles.seeAll}>See All</Text>
            </TouchableOpacity>
          </View>

          {TODAYS_APPOINTMENTS.map((appointment) => (
            <TouchableOpacity
              key={appointment.id}
              style={styles.appointmentCard}
              onPress={() =>
                navigation.navigate('AppointmentDetail' as never, {
                  appointmentId: appointment.id,
                } as never)
              }
            >
              <View style={styles.appointmentTime}>
                <Text style={styles.timeText}>{appointment.time}</Text>
                <View
                  style={[
                    styles.statusDot,
                    { backgroundColor: getStatusColor(appointment.status) },
                  ]}
                />
              </View>
              <View style={styles.appointmentDetails}>
                <Text style={styles.patientName}>{appointment.patientName}</Text>
                <Text style={styles.appointmentReason}>{appointment.reason}</Text>
                <View style={styles.appointmentMeta}>
                  <View
                    style={[
                      styles.appointmentType,
                      {
                        backgroundColor:
                          appointment.type === 'Video' ? '#dbeafe' : '#dcfce7',
                      },
                    ]}
                  >
                    <Ionicons
                      name={appointment.type === 'Video' ? 'videocam' : 'location'}
                      size={12}
                      color={appointment.type === 'Video' ? '#2563eb' : '#16a34a'}
                    />
                    <Text
                      style={{
                        marginLeft: 4,
                        fontSize: 12,
                        color: appointment.type === 'Video' ? '#2563eb' : '#16a34a',
                      }}
                    >
                      {appointment.type}
                    </Text>
                  </View>
                </View>
              </View>
              {appointment.status === 'in-progress' && (
                <TouchableOpacity style={styles.joinButton}>
                  <Ionicons name="videocam" size={16} color="white" />
                  <Text style={styles.joinButtonText}>Join</Text>
                </TouchableOpacity>
              )}
            </TouchableOpacity>
          ))}
        </View>

        {/* Appointments Chart */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>This Week's Appointments</Text>
          <View style={styles.chartContainer}>
            <LineChart
              data={chartData}
              width={screenWidth - 60}
              height={180}
              chartConfig={{
                backgroundColor: '#ffffff',
                backgroundGradientFrom: '#ffffff',
                backgroundGradientTo: '#ffffff',
                decimalPlaces: 0,
                color: (opacity = 1) => `rgba(5, 150, 105, ${opacity})`,
                labelColor: () => '#6b7280',
                style: { borderRadius: 16 },
                propsForDots: {
                  r: '4',
                  strokeWidth: '2',
                  stroke: '#059669',
                },
              }}
              bezier
              style={styles.chart}
            />
          </View>
        </View>

        {/* Quick Actions */}
        <View style={styles.section}>
          <Text style={styles.sectionTitle}>Quick Actions</Text>
          <View style={styles.quickActions}>
            <TouchableOpacity style={styles.quickAction}>
              <View style={[styles.quickActionIcon, { backgroundColor: '#dbeafe' }]}>
                <Ionicons name="create-outline" size={24} color="#2563eb" />
              </View>
              <Text style={styles.quickActionText}>Write Prescription</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={styles.quickAction}
              onPress={() => navigation.navigate('Reviews' as never)}
            >
              <View style={[styles.quickActionIcon, { backgroundColor: '#fef3c7' }]}>
                <Ionicons name="star-outline" size={24} color="#d97706" />
              </View>
              <Text style={styles.quickActionText}>View Reviews</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={styles.quickAction}
              onPress={() => navigation.navigate('Settings' as never)}
            >
              <View style={[styles.quickActionIcon, { backgroundColor: '#f3e8ff' }]}>
                <Ionicons name="settings-outline" size={24} color="#7c3aed" />
              </View>
              <Text style={styles.quickActionText}>Settings</Text>
            </TouchableOpacity>
          </View>
        </View>
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
  headerContent: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 24,
  },
  greeting: {
    fontSize: 16,
    color: 'rgba(255,255,255,0.8)',
  },
  doctorName: {
    fontSize: 24,
    fontWeight: 'bold',
    color: 'white',
  },
  notificationBtn: {
    width: 44,
    height: 44,
    borderRadius: 22,
    backgroundColor: 'rgba(255,255,255,0.2)',
    alignItems: 'center',
    justifyContent: 'center',
  },
  notificationBadge: {
    position: 'absolute',
    top: -2,
    right: -2,
    backgroundColor: '#ef4444',
    borderRadius: 10,
    width: 20,
    height: 20,
    alignItems: 'center',
    justifyContent: 'center',
  },
  badgeText: {
    color: 'white',
    fontSize: 12,
    fontWeight: 'bold',
  },
  quickStats: {
    flexDirection: 'row',
    backgroundColor: 'rgba(255,255,255,0.15)',
    borderRadius: 16,
    padding: 16,
  },
  quickStatItem: {
    flex: 1,
    alignItems: 'center',
  },
  quickStatValue: {
    fontSize: 20,
    fontWeight: 'bold',
    color: 'white',
  },
  quickStatLabel: {
    fontSize: 12,
    color: 'rgba(255,255,255,0.8)',
    marginTop: 4,
  },
  quickStatDivider: {
    width: 1,
    backgroundColor: 'rgba(255,255,255,0.3)',
    marginHorizontal: 16,
  },
  statsContainer: {
    padding: 20,
    marginTop: -20,
  },
  statsRow: {
    flexDirection: 'row',
    gap: 12,
  },
  statCard: {
    flex: 1,
    borderRadius: 16,
    padding: 16,
    alignItems: 'center',
  },
  statValue: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#111827',
    marginTop: 8,
  },
  statLabel: {
    fontSize: 12,
    color: '#6b7280',
    marginTop: 4,
  },
  section: {
    paddingHorizontal: 20,
    marginBottom: 24,
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
    color: '#059669',
  },
  appointmentCard: {
    flexDirection: 'row',
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
  appointmentTime: {
    alignItems: 'center',
    marginRight: 16,
    width: 70,
  },
  timeText: {
    fontSize: 14,
    fontWeight: '600',
    color: '#111827',
  },
  statusDot: {
    width: 8,
    height: 8,
    borderRadius: 4,
    marginTop: 8,
  },
  appointmentDetails: {
    flex: 1,
  },
  patientName: {
    fontSize: 16,
    fontWeight: '600',
    color: '#111827',
  },
  appointmentReason: {
    fontSize: 14,
    color: '#6b7280',
    marginTop: 4,
  },
  appointmentMeta: {
    flexDirection: 'row',
    marginTop: 8,
  },
  appointmentType: {
    flexDirection: 'row',
    alignItems: 'center',
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 8,
  },
  joinButton: {
    flexDirection: 'row',
    alignItems: 'center',
    backgroundColor: '#059669',
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 8,
    alignSelf: 'center',
  },
  joinButtonText: {
    color: 'white',
    fontWeight: '600',
    marginLeft: 4,
  },
  chartContainer: {
    backgroundColor: 'white',
    borderRadius: 16,
    padding: 16,
    marginTop: 12,
  },
  chart: {
    marginVertical: 8,
    borderRadius: 16,
  },
  quickActions: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    backgroundColor: 'white',
    borderRadius: 16,
    padding: 20,
    marginTop: 12,
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
    textAlign: 'center',
  },
});
