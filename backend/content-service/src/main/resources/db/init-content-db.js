// MongoDB initialization script for content-service
// Run with: mongosh < init-content-db.js

// Switch to content database
use healthapp_content;

// Create collections with validation
db.createCollection("articles", {
   validator: {
      $jsonSchema: {
         bsonType: "object",
         required: ["title", "slug", "status"],
         properties: {
            title: {
               bsonType: "string",
               description: "Article title is required"
            },
            slug: {
               bsonType: "string",
               description: "URL-friendly slug is required"
            },
            status: {
               enum: ["DRAFT", "REVIEW", "PUBLISHED", "ARCHIVED"],
               description: "Status must be valid"
            }
         }
      }
   }
});

// Create indexes for articles
db.articles.createIndex({ "slug": 1 }, { unique: true });
db.articles.createIndex({ "status": 1, "publishedAt": -1 });
db.articles.createIndex({ "category.id": 1 });
db.articles.createIndex({ "tags": 1 });
db.articles.createIndex({ "author.doctorId": 1 });
db.articles.createIndex({ "isFeatured": 1, "status": 1 });
db.articles.createIndex({ "isEditorsPick": 1, "status": 1 });
db.articles.createIndex({ 
    "title": "text", 
    "content": "text", 
    "tags": "text" 
}, {
    weights: {
        title: 10,
        tags: 5,
        content: 1
    },
    name: "article_text_search"
});

// Create article_categories collection
db.createCollection("article_categories");
db.article_categories.createIndex({ "categoryId": 1 }, { unique: true });
db.article_categories.createIndex({ "slug": 1 }, { unique: true });
db.article_categories.createIndex({ "parentCategoryId": 1 });
db.article_categories.createIndex({ "order": 1 });

// Create article_likes collection
db.createCollection("article_likes");
db.article_likes.createIndex({ "articleId": 1, "userId": 1 }, { unique: true });
db.article_likes.createIndex({ "userId": 1 });

// Create article_bookmarks collection
db.createCollection("article_bookmarks");
db.article_bookmarks.createIndex({ "articleId": 1, "userId": 1 }, { unique: true });
db.article_bookmarks.createIndex({ "userId": 1, "createdAt": -1 });

// Create article_comments collection
db.createCollection("article_comments");
db.article_comments.createIndex({ "articleId": 1, "createdAt": -1 });
db.article_comments.createIndex({ "parentCommentId": 1 });
db.article_comments.createIndex({ "userId": 1 });
db.article_comments.createIndex({ "isApproved": 1, "createdAt": 1 });

// Insert sample categories
db.article_categories.insertMany([
    {
        categoryId: "cardiology",
        name: "Heart Health",
        description: "Articles about cardiovascular health and heart conditions",
        icon: "heart",
        color: "#e74c3c",
        slug: "heart-health",
        parentCategoryId: null,
        articleCount: 0,
        order: 1,
        isActive: true
    },
    {
        categoryId: "diabetes",
        name: "Diabetes",
        description: "Information about diabetes management and prevention",
        icon: "activity",
        color: "#3498db",
        slug: "diabetes",
        parentCategoryId: null,
        articleCount: 0,
        order: 2,
        isActive: true
    },
    {
        categoryId: "mental-health",
        name: "Mental Health",
        description: "Mental wellness, stress management, and psychological health",
        icon: "brain",
        color: "#9b59b6",
        slug: "mental-health",
        parentCategoryId: null,
        articleCount: 0,
        order: 3,
        isActive: true
    },
    {
        categoryId: "nutrition",
        name: "Nutrition & Diet",
        description: "Healthy eating, diet plans, and nutritional advice",
        icon: "apple",
        color: "#27ae60",
        slug: "nutrition-diet",
        parentCategoryId: null,
        articleCount: 0,
        order: 4,
        isActive: true
    },
    {
        categoryId: "fitness",
        name: "Fitness & Exercise",
        description: "Workout routines, fitness tips, and physical activity",
        icon: "dumbbell",
        color: "#f39c12",
        slug: "fitness-exercise",
        parentCategoryId: null,
        articleCount: 0,
        order: 5,
        isActive: true
    },
    {
        categoryId: "womens-health",
        name: "Women's Health",
        description: "Health topics specific to women",
        icon: "female",
        color: "#e91e63",
        slug: "womens-health",
        parentCategoryId: null,
        articleCount: 0,
        order: 6,
        isActive: true
    },
    {
        categoryId: "pediatrics",
        name: "Child Health",
        description: "Health information for infants, children, and adolescents",
        icon: "baby",
        color: "#00bcd4",
        slug: "child-health",
        parentCategoryId: null,
        articleCount: 0,
        order: 7,
        isActive: true
    },
    {
        categoryId: "preventive-care",
        name: "Preventive Care",
        description: "Health screenings, vaccinations, and preventive measures",
        icon: "shield",
        color: "#4caf50",
        slug: "preventive-care",
        parentCategoryId: null,
        articleCount: 0,
        order: 8,
        isActive: true
    }
]);

// Insert sample article
db.articles.insertOne({
    slug: "10-tips-for-healthy-heart-abc12345",
    title: "10 Tips for a Healthy Heart",
    subtitle: "Simple lifestyle changes that can make a big difference",
    content: "# 10 Tips for a Healthy Heart\n\nMaintaining a healthy heart is essential for overall wellbeing...",
    excerpt: "Learn simple lifestyle changes that can significantly improve your cardiovascular health and reduce the risk of heart disease.",
    featuredImage: {
        url: "https://cdn.healthapp.com/articles/heart-health.jpg",
        alt: "Healthy heart illustration",
        caption: "Image source: HealthApp"
    },
    author: {
        type: "DOCTOR",
        doctorId: "11111111-1111-1111-1111-111111111111",
        name: "Dr. Sarah Smith",
        avatar: "https://cdn.healthapp.com/doctors/sarah-smith.jpg",
        specialization: "Cardiology",
        credentials: "MD, FACC"
    },
    category: {
        id: "cardiology",
        name: "Heart Health"
    },
    tags: ["heart", "lifestyle", "prevention", "diet", "exercise"],
    seo: {
        metaTitle: "10 Tips for a Healthy Heart | HealthApp",
        metaDescription: "Learn simple lifestyle changes that can significantly improve your cardiovascular health.",
        keywords: ["heart health", "cardiovascular", "healthy living"]
    },
    status: "PUBLISHED",
    publishedAt: new Date(),
    readTimeMinutes: 5,
    difficulty: "BEGINNER",
    stats: {
        views: 1250,
        uniqueViews: 890,
        likes: 156,
        shares: 45,
        bookmarks: 89,
        comments: 23,
        avgReadTime: 4.2
    },
    isFeatured: true,
    isEditorsPick: true,
    isPremium: false,
    medicalReview: {
        reviewedBy: "Dr. John Doe",
        reviewedAt: new Date(),
        nextReviewDate: new Date(Date.now() + 365 * 24 * 60 * 60 * 1000)
    },
    createdAt: new Date(),
    updatedAt: new Date(),
    createdBy: "admin",
    version: 1
});

print("Content database initialized successfully!");
