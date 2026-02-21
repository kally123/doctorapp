#!/bin/bash
# MongoDB initialization script for multiple databases
# This script runs when MongoDB container first starts

set -e

# Wait for MongoDB to be ready
until mongosh --host localhost -u admin -p mongo_password --authenticationDatabase admin --eval "db.adminCommand('ping')" > /dev/null 2>&1; do
  echo "Waiting for MongoDB to be ready..."
  sleep 2
done

echo "MongoDB is ready. Initializing databases..."

# Initialize content database
mongosh --host localhost -u admin -p mongo_password --authenticationDatabase admin <<EOF
use healthapp_content;

// Create collections with validation
db.createCollection("articles", {
   validator: {
      \$jsonSchema: {
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
    }
]);

print("Content database initialized successfully!");
EOF

echo "MongoDB initialization completed!"

