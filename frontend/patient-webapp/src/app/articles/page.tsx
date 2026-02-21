import { Metadata } from 'next';
import { Suspense } from 'react';
import ArticleList from '@/components/articles/ArticleList';
import CategorySidebar from '@/components/articles/CategorySidebar';
import FeaturedArticles from '@/components/articles/FeaturedArticles';

export const metadata: Metadata = {
  title: 'Health Articles | HealthApp',
  description: 'Expert health articles, tips, and insights from verified medical professionals.',
};

async function getArticles() {
  // This would fetch from the content service
  return {
    featured: [
      {
        id: '1',
        slug: '10-tips-healthy-heart',
        title: '10 Tips for a Healthy Heart',
        excerpt: 'Learn simple lifestyle changes that can significantly improve your cardiovascular health.',
        featuredImage: 'https://cdn.healthapp.com/articles/heart-health.jpg',
        author: {
          name: 'Dr. Sarah Smith',
          avatar: 'https://cdn.healthapp.com/doctors/sarah.jpg',
          specialization: 'Cardiology',
        },
        category: { id: 'cardiology', name: 'Heart Health' },
        readTimeMinutes: 5,
        publishedAt: '2024-01-15',
        stats: { views: 1250, likes: 156 },
      },
      {
        id: '2',
        slug: 'managing-diabetes-diet',
        title: 'Managing Diabetes Through Diet',
        excerpt: 'Discover how dietary choices can help control blood sugar levels effectively.',
        featuredImage: 'https://cdn.healthapp.com/articles/diabetes-diet.jpg',
        author: {
          name: 'Dr. Michael Chen',
          avatar: 'https://cdn.healthapp.com/doctors/michael.jpg',
          specialization: 'Endocrinology',
        },
        category: { id: 'diabetes', name: 'Diabetes' },
        readTimeMinutes: 7,
        publishedAt: '2024-01-14',
        stats: { views: 890, likes: 98 },
      },
    ],
    articles: [],
    categories: [
      { id: 'cardiology', name: 'Heart Health', slug: 'heart-health', icon: 'heart', articleCount: 45 },
      { id: 'diabetes', name: 'Diabetes', slug: 'diabetes', icon: 'activity', articleCount: 32 },
      { id: 'mental-health', name: 'Mental Health', slug: 'mental-health', icon: 'brain', articleCount: 28 },
      { id: 'nutrition', name: 'Nutrition & Diet', slug: 'nutrition-diet', icon: 'apple', articleCount: 56 },
      { id: 'fitness', name: 'Fitness', slug: 'fitness-exercise', icon: 'dumbbell', articleCount: 41 },
    ],
  };
}

export default async function ArticlesPage() {
  const data = await getArticles();

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Hero Section */}
      <div className="bg-gradient-to-r from-blue-600 to-blue-800 text-white py-16">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <h1 className="text-4xl font-bold mb-4">Health Articles</h1>
          <p className="text-xl text-blue-100 max-w-2xl">
            Expert health insights, tips, and guidance from verified medical professionals
            to help you live a healthier life.
          </p>
          
          {/* Search Bar */}
          <div className="mt-8 max-w-xl">
            <div className="relative">
              <input
                type="text"
                placeholder="Search articles..."
                className="w-full px-5 py-3 pr-12 rounded-lg text-gray-900 placeholder-gray-500 focus:outline-none focus:ring-2 focus:ring-blue-300"
              />
              <button className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-600">
                <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
                </svg>
              </button>
            </div>
          </div>
        </div>
      </div>

      {/* Featured Articles */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 -mt-8">
        <FeaturedArticles articles={data.featured} />
      </div>

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="flex flex-col lg:flex-row gap-8">
          {/* Sidebar */}
          <aside className="lg:w-64 flex-shrink-0">
            <Suspense fallback={
              <div className="bg-white rounded-xl shadow-sm p-5 animate-pulse">
                <div className="h-6 bg-gray-200 rounded w-32 mb-4"></div>
                <div className="space-y-2">
                  {[1, 2, 3, 4, 5].map((n) => (
                    <div key={n} className="h-10 bg-gray-200 rounded"></div>
                  ))}
                </div>
              </div>
            }>
              <CategorySidebar categories={data.categories} />
            </Suspense>
          </aside>

          {/* Articles Grid */}
          <main className="flex-1">
            <Suspense fallback={
              <div className="space-y-6">
                {[1, 2, 3].map((n) => (
                  <div key={n} className="bg-white rounded-xl p-4 animate-pulse">
                    <div className="h-48 bg-gray-200 rounded-lg mb-4"></div>
                    <div className="h-6 bg-gray-200 rounded w-3/4 mb-2"></div>
                    <div className="h-4 bg-gray-200 rounded w-full mb-2"></div>
                    <div className="h-4 bg-gray-200 rounded w-2/3"></div>
                  </div>
                ))}
              </div>
            }>
              <ArticleList />
            </Suspense>
          </main>
        </div>
      </div>
    </div>
  );
}
