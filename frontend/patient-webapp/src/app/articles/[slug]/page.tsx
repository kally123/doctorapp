import { Metadata } from 'next';
import { notFound } from 'next/navigation';
import ArticleContent from '@/components/articles/ArticleContent';
import ArticleSidebar from '@/components/articles/ArticleSidebar';
import RelatedArticles from '@/components/articles/RelatedArticles';

interface ArticlePageProps {
  params: { slug: string };
}

async function getArticle(slug: string) {
  // This would fetch from content service
  // For demo, return mock data
  return {
    id: '1',
    slug: slug,
    title: '10 Tips for a Healthy Heart',
    subtitle: 'Simple lifestyle changes that can make a big difference',
    content: `
# 10 Tips for a Healthy Heart

Maintaining cardiovascular health is one of the most important things you can do for your overall wellbeing. Heart disease remains the leading cause of death worldwide, but many risk factors are within your control.

## 1. Eat a Heart-Healthy Diet

Focus on:
- **Fruits and vegetables**: Aim for at least 5 servings daily
- **Whole grains**: Choose whole wheat, oats, and brown rice
- **Lean proteins**: Fish, poultry, beans, and legumes
- **Healthy fats**: Olive oil, avocados, and nuts

Limit:
- Saturated and trans fats
- Sodium (less than 2,300mg daily)
- Added sugars
- Processed foods

## 2. Exercise Regularly

Aim for at least 150 minutes of moderate-intensity aerobic activity per week, such as:
- Brisk walking
- Swimming
- Cycling
- Dancing

## 3. Maintain a Healthy Weight

Being overweight increases your risk of heart disease. Calculate your BMI and work with your healthcare provider to reach a healthy weight.

## 4. Quit Smoking

Smoking damages blood vessels and significantly increases heart disease risk. If you smoke, quitting is the best thing you can do for your heart.

## 5. Limit Alcohol

If you drink alcohol, do so in moderation:
- Men: Up to 2 drinks per day
- Women: Up to 1 drink per day

## 6. Manage Stress

Chronic stress can contribute to heart disease. Try:
- Meditation and deep breathing
- Regular physical activity
- Spending time with loved ones
- Getting enough sleep

## 7. Monitor Blood Pressure

High blood pressure often has no symptoms. Get regular checkups and work with your doctor to manage it if elevated.

## 8. Control Cholesterol

Know your numbers. High cholesterol can lead to plaque buildup in arteries. Get tested and follow your doctor's recommendations.

## 9. Manage Diabetes

If you have diabetes, controlling blood sugar is crucial for heart health. Work closely with your healthcare team.

## 10. Get Enough Sleep

Aim for 7-9 hours of quality sleep per night. Poor sleep is linked to higher risk of heart disease.

---

## When to See a Doctor

Contact your healthcare provider if you experience:
- Chest pain or discomfort
- Shortness of breath
- Unusual fatigue
- Heart palpitations

Regular check-ups are important for early detection and prevention.

*This article is for informational purposes only and should not replace professional medical advice.*
    `,
    excerpt: 'Learn simple lifestyle changes that can significantly improve your cardiovascular health and reduce the risk of heart disease.',
    featuredImage: {
      url: 'https://cdn.healthapp.com/articles/heart-health.jpg',
      alt: 'Healthy heart illustration',
      caption: 'Image source: HealthApp',
    },
    author: {
      type: 'DOCTOR',
      doctorId: '11111111-1111-1111-1111-111111111111',
      name: 'Dr. Sarah Smith',
      avatar: 'https://cdn.healthapp.com/doctors/sarah.jpg',
      specialization: 'Cardiology',
      credentials: 'MD, FACC',
    },
    category: { id: 'cardiology', name: 'Heart Health' },
    tags: ['heart', 'lifestyle', 'prevention', 'diet', 'exercise'],
    seo: {
      metaTitle: '10 Tips for a Healthy Heart | HealthApp',
      metaDescription: 'Learn simple lifestyle changes that can significantly improve your cardiovascular health.',
    },
    publishedAt: '2024-01-15T10:00:00Z',
    readTimeMinutes: 5,
    difficulty: 'BEGINNER',
    stats: {
      views: 1250,
      likes: 156,
      bookmarks: 89,
      comments: 23,
    },
    isFeatured: true,
    medicalReview: {
      reviewedBy: 'Dr. John Doe, MD',
      reviewedAt: '2024-01-10T10:00:00Z',
    },
  };
}

async function getRelatedArticles(articleId: string, categoryId: string) {
  return [
    {
      id: '2',
      slug: 'understanding-cholesterol',
      title: 'Understanding Your Cholesterol Numbers',
      excerpt: 'What your cholesterol test results mean and how to improve them.',
      featuredImage: 'https://cdn.healthapp.com/articles/cholesterol.jpg',
      readTimeMinutes: 4,
      category: { name: 'Heart Health' },
    },
    {
      id: '3',
      slug: 'exercise-heart-health',
      title: 'Best Exercises for Heart Health',
      excerpt: 'Cardio workouts that strengthen your heart muscle.',
      featuredImage: 'https://cdn.healthapp.com/articles/cardio.jpg',
      readTimeMinutes: 6,
      category: { name: 'Heart Health' },
    },
    {
      id: '4',
      slug: 'heart-healthy-recipes',
      title: '15 Heart-Healthy Recipes',
      excerpt: 'Delicious meals that are good for your cardiovascular system.',
      featuredImage: 'https://cdn.healthapp.com/articles/recipes.jpg',
      readTimeMinutes: 8,
      category: { name: 'Nutrition' },
    },
  ];
}

export async function generateMetadata({ params }: ArticlePageProps): Promise<Metadata> {
  const article = await getArticle(params.slug);
  
  if (!article) {
    return { title: 'Article Not Found' };
  }

  return {
    title: article.seo.metaTitle,
    description: article.seo.metaDescription,
    openGraph: {
      title: article.title,
      description: article.excerpt,
      images: [article.featuredImage.url],
      type: 'article',
      publishedTime: article.publishedAt,
      authors: [article.author.name],
    },
  };
}

export default async function ArticlePage({ params }: ArticlePageProps) {
  const article = await getArticle(params.slug);
  
  if (!article) {
    notFound();
  }

  const relatedArticles = await getRelatedArticles(article.id, article.category.id);

  return (
    <div className="min-h-screen bg-gray-50">
      {/* Article Header */}
      <div className="bg-white border-b">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 py-8">
          {/* Breadcrumb */}
          <nav className="text-sm text-gray-500 mb-4">
            <a href="/articles" className="hover:text-blue-600">Articles</a>
            <span className="mx-2">/</span>
            <a href={`/articles/category/${article.category.id}`} className="hover:text-blue-600">
              {article.category.name}
            </a>
          </nav>

          {/* Title */}
          <h1 className="text-3xl md:text-4xl font-bold text-gray-900 mb-4">
            {article.title}
          </h1>

          {article.subtitle && (
            <p className="text-xl text-gray-600 mb-6">{article.subtitle}</p>
          )}

          {/* Author & Meta */}
          <div className="flex flex-wrap items-center gap-4 text-sm text-gray-500">
            <div className="flex items-center gap-3">
              <img
                src={article.author.avatar}
                alt={article.author.name}
                className="w-10 h-10 rounded-full"
              />
              <div>
                <div className="font-medium text-gray-900">{article.author.name}</div>
                <div>{article.author.specialization}</div>
              </div>
            </div>
            <span className="hidden sm:inline">•</span>
            <span>{new Date(article.publishedAt).toLocaleDateString('en-US', {
              year: 'numeric',
              month: 'long',
              day: 'numeric',
            })}</span>
            <span>•</span>
            <span>{article.readTimeMinutes} min read</span>
          </div>
        </div>
      </div>

      {/* Featured Image */}
      {article.featuredImage && (
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8">
          <figure className="-mt-4">
            <img
              src={article.featuredImage.url}
              alt={article.featuredImage.alt}
              className="w-full h-64 md:h-96 object-cover rounded-lg shadow-lg"
            />
            {article.featuredImage.caption && (
              <figcaption className="text-center text-sm text-gray-500 mt-2">
                {article.featuredImage.caption}
              </figcaption>
            )}
          </figure>
        </div>
      )}

      {/* Main Content */}
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-12">
        <div className="flex flex-col lg:flex-row gap-8">
          {/* Article Content */}
          <main className="flex-1 max-w-4xl">
            <ArticleContent article={article} />
          </main>

          {/* Sidebar */}
          <aside className="lg:w-72 flex-shrink-0">
            <ArticleSidebar article={article} />
          </aside>
        </div>
      </div>

      {/* Related Articles */}
      <div className="bg-white border-t py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <RelatedArticles articles={relatedArticles} />
        </div>
      </div>
    </div>
  );
}
