'use client';

import React, { useState, useEffect } from 'react';
import Link from 'next/link';
import { useSearchParams } from 'next/navigation';
import { Clock, Eye, Heart, Bookmark, BookmarkCheck } from 'lucide-react';

interface Article {
  id: string;
  slug: string;
  title: string;
  excerpt: string;
  featuredImage: string;
  author: {
    name: string;
    avatar: string;
    specialization: string;
  };
  category: { id: string; name: string };
  tags: string[];
  readTimeMinutes: number;
  publishedAt: string;
  stats: { views: number; likes: number; bookmarks: number };
  isBookmarked?: boolean;
}

export default function ArticleList() {
  const searchParams = useSearchParams();
  const category = searchParams.get('category');
  const tag = searchParams.get('tag');
  const search = searchParams.get('q');

  const [articles, setArticles] = useState<Article[]>([]);
  const [loading, setLoading] = useState(true);
  const [page, setPage] = useState(0);
  const [hasMore, setHasMore] = useState(true);

  useEffect(() => {
    fetchArticles();
  }, [category, tag, search]);

  const fetchArticles = async () => {
    setLoading(true);
    try {
      // This would call the content service API
      // const response = await fetch(`/api/v1/articles?category=${category}&tag=${tag}&search=${search}&page=${page}`);
      // const data = await response.json();
      
      // Mock data for demo
      const mockArticles: Article[] = [
        {
          id: '1',
          slug: 'understanding-blood-pressure',
          title: 'Understanding Your Blood Pressure Numbers',
          excerpt: 'Learn what systolic and diastolic readings mean and how to maintain healthy levels.',
          featuredImage: 'https://cdn.healthapp.com/articles/blood-pressure.jpg',
          author: { name: 'Dr. James Wilson', avatar: '', specialization: 'Internal Medicine' },
          category: { id: 'cardiology', name: 'Heart Health' },
          tags: ['blood pressure', 'heart', 'monitoring'],
          readTimeMinutes: 6,
          publishedAt: '2024-01-12',
          stats: { views: 2340, likes: 189, bookmarks: 67 },
        },
        {
          id: '2',
          slug: 'stress-management-techniques',
          title: '10 Science-Backed Stress Management Techniques',
          excerpt: 'Discover proven methods to reduce stress and improve your mental wellbeing.',
          featuredImage: 'https://cdn.healthapp.com/articles/stress.jpg',
          author: { name: 'Dr. Emily Brown', avatar: '', specialization: 'Psychology' },
          category: { id: 'mental-health', name: 'Mental Health' },
          tags: ['stress', 'mental health', 'wellness'],
          readTimeMinutes: 8,
          publishedAt: '2024-01-11',
          stats: { views: 1890, likes: 234, bookmarks: 112 },
        },
        {
          id: '3',
          slug: 'healthy-meal-prep-guide',
          title: 'The Complete Guide to Healthy Meal Prep',
          excerpt: 'Save time and eat better with these meal preparation strategies and recipes.',
          featuredImage: 'https://cdn.healthapp.com/articles/meal-prep.jpg',
          author: { name: 'Dr. Lisa Chen', avatar: '', specialization: 'Nutrition' },
          category: { id: 'nutrition', name: 'Nutrition & Diet' },
          tags: ['nutrition', 'meal prep', 'cooking'],
          readTimeMinutes: 10,
          publishedAt: '2024-01-10',
          stats: { views: 3210, likes: 412, bookmarks: 289 },
        },
        {
          id: '4',
          slug: 'beginners-guide-to-running',
          title: "Beginner's Guide to Running: Start Your Journey",
          excerpt: 'Everything you need to know to start running safely and effectively.',
          featuredImage: 'https://cdn.healthapp.com/articles/running.jpg',
          author: { name: 'Dr. Mark Taylor', avatar: '', specialization: 'Sports Medicine' },
          category: { id: 'fitness', name: 'Fitness & Exercise' },
          tags: ['running', 'exercise', 'fitness'],
          readTimeMinutes: 7,
          publishedAt: '2024-01-09',
          stats: { views: 1567, likes: 198, bookmarks: 87 },
        },
        {
          id: '5',
          slug: 'improving-sleep-quality',
          title: 'How to Improve Your Sleep Quality Tonight',
          excerpt: 'Simple changes you can make today for better sleep and more energy.',
          featuredImage: 'https://cdn.healthapp.com/articles/sleep.jpg',
          author: { name: 'Dr. Sarah Adams', avatar: '', specialization: 'Sleep Medicine' },
          category: { id: 'mental-health', name: 'Mental Health' },
          tags: ['sleep', 'wellness', 'rest'],
          readTimeMinutes: 5,
          publishedAt: '2024-01-08',
          stats: { views: 2890, likes: 356, bookmarks: 178 },
        },
      ];

      setArticles(mockArticles);
      setHasMore(false);
    } catch (error) {
      console.error('Failed to fetch articles:', error);
    } finally {
      setLoading(false);
    }
  };

  const toggleBookmark = async (articleId: string) => {
    try {
      // await fetch(`/api/v1/articles/${articleId}/bookmark`, { method: 'POST' });
      setArticles(articles.map(a => 
        a.id === articleId ? { ...a, isBookmarked: !a.isBookmarked } : a
      ));
    } catch (error) {
      console.error('Failed to toggle bookmark:', error);
    }
  };

  const formatDate = (dateString: string) => {
    return new Date(dateString).toLocaleDateString('en-US', {
      year: 'numeric',
      month: 'short',
      day: 'numeric',
    });
  };

  if (loading) {
    return (
      <div className="space-y-6">
        {[1, 2, 3].map((n) => (
          <div key={n} className="bg-white rounded-xl p-4 animate-pulse">
            <div className="flex gap-4">
              <div className="w-48 h-32 bg-gray-200 rounded-lg" />
              <div className="flex-1">
                <div className="h-4 bg-gray-200 rounded w-1/4 mb-2" />
                <div className="h-6 bg-gray-200 rounded w-3/4 mb-2" />
                <div className="h-4 bg-gray-200 rounded w-full mb-4" />
                <div className="h-4 bg-gray-200 rounded w-1/2" />
              </div>
            </div>
          </div>
        ))}
      </div>
    );
  }

  return (
    <div>
      {/* Sort Options */}
      <div className="flex items-center justify-between mb-6">
        <h2 className="text-xl font-semibold text-gray-900">
          {category ? `${category} Articles` : tag ? `#${tag}` : 'Latest Articles'}
        </h2>
        <select className="border border-gray-300 rounded-lg px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-blue-500">
          <option value="recent">Most Recent</option>
          <option value="popular">Most Popular</option>
          <option value="trending">Trending</option>
        </select>
      </div>

      {/* Articles */}
      <div className="space-y-6">
        {articles.map((article) => (
          <article
            key={article.id}
            className="bg-white rounded-xl shadow-sm overflow-hidden hover:shadow-md transition-shadow"
          >
            <div className="flex flex-col sm:flex-row">
              {/* Image */}
              <Link href={`/articles/${article.slug}`} className="sm:w-48 flex-shrink-0">
                <img
                  src={article.featuredImage}
                  alt={article.title}
                  className="w-full h-48 sm:h-full object-cover"
                />
              </Link>

              {/* Content */}
              <div className="flex-1 p-5">
                <div className="flex items-center gap-2 mb-2">
                  <Link
                    href={`/articles?category=${article.category.id}`}
                    className="text-sm text-blue-600 hover:underline"
                  >
                    {article.category.name}
                  </Link>
                  <span className="text-gray-300">•</span>
                  <span className="text-sm text-gray-500">{formatDate(article.publishedAt)}</span>
                </div>

                <Link href={`/articles/${article.slug}`}>
                  <h3 className="text-lg font-semibold text-gray-900 hover:text-blue-600 transition mb-2">
                    {article.title}
                  </h3>
                </Link>

                <p className="text-gray-600 text-sm mb-4 line-clamp-2">
                  {article.excerpt}
                </p>

                <div className="flex items-center justify-between">
                  <div className="flex items-center gap-4 text-sm text-gray-500">
                    <span className="flex items-center gap-1">
                      <Clock className="w-4 h-4" />
                      {article.readTimeMinutes} min
                    </span>
                    <span className="flex items-center gap-1">
                      <Eye className="w-4 h-4" />
                      {article.stats.views.toLocaleString()}
                    </span>
                    <span className="flex items-center gap-1">
                      <Heart className="w-4 h-4" />
                      {article.stats.likes}
                    </span>
                  </div>

                  <button
                    onClick={(e) => {
                      e.preventDefault();
                      toggleBookmark(article.id);
                    }}
                    className={`p-2 rounded-full transition ${
                      article.isBookmarked
                        ? 'text-blue-600 bg-blue-50'
                        : 'text-gray-400 hover:bg-gray-100'
                    }`}
                  >
                    {article.isBookmarked ? (
                      <BookmarkCheck className="w-5 h-5" />
                    ) : (
                      <Bookmark className="w-5 h-5" />
                    )}
                  </button>
                </div>
              </div>
            </div>
          </article>
        ))}
      </div>

      {/* Load More */}
      {hasMore && (
        <div className="text-center mt-8">
          <button
            onClick={() => setPage(page + 1)}
            className="px-6 py-2 bg-blue-600 text-white rounded-lg hover:bg-blue-700 transition"
          >
            Load More Articles
          </button>
        </div>
      )}
    </div>
  );
}
