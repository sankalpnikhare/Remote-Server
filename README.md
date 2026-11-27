# 🏨 StayEase — Hotel Reservation System

**StayEase** is a full-stack hotel reservation platform built using **Node.js, Express.js, and MongoDB**. It allows users to explore and reserve hotels across different cities, while property owners can list their properties and manage reservations through a dedicated host dashboard.

The platform provides secure authentication, email verification, cloud-based image storage, and booking request management to deliver a smooth experience for both guests and hosts.

## ✨ Features

### 👤 User Authentication & Security
- **Google OAuth 2.0** — Sign in using Google authentication.
- **JWT Authentication** — Secure token-based authentication.
- **Email Verification** — Verify user email addresses during registration.
- **Login Rate Limiting** — Protect login endpoints against brute-force attacks.
- Secure password hashing and authentication.

### 🏨 Hotel Reservations
- Explore hotels listed across multiple cities.
- View property details, pricing, and available information.
- Submit hotel reservation requests.
- Track booking request status.

### 🏠 Property Management
- List your own properties on the platform.
- Upload multiple property images.
- Cloud-based image storage and CDN delivery.
- Manage listed properties through a dedicated host dashboard.

### 📊 Host Dashboard
- View all listed properties.
- Track booked properties.
- View incoming reservation requests.
- **Confirm or Reject** reservation requests.
- Manage property-related booking activity from one place.

## 🛠️ Tech Stack

| Technology | Purpose |
|---|---|
| Node.js | Backend JavaScript runtime |
| Express.js | Server-side framework and REST API handling |
| MongoDB | Database |
| Mongoose | MongoDB object modeling |
| HTML5 | Frontend structure |
| CSS3 | Frontend styling |
| JavaScript | Client-side functionality |
| JWT | Token-based authentication |
| Google OAuth 2.0 | Google authentication |
| Cloudinary | Cloud image storage and CDN delivery |
| bcrypt | Password hashing |
| Nodemailer | Email verification and notifications |

## ⚙️ Getting Started

Follow these steps to run StayEase locally.

### 1. Clone the Repository

```bash
git clone https://github.com/YOUR_USERNAME/stayease.git
cd stayease
```

### 2. Install Dependencies

```bash
npm install
```

### 3. Configure Environment Variables

Create a `.env` file in the root directory.

```env
PORT=5000

MONGO_URI=your_mongodb_connection_string

JWT_SECRET=your_jwt_secret

GOOGLE_CLIENT_ID=your_google_client_id
GOOGLE_CLIENT_SECRET=your_google_client_secret

EMAIL_USER=your_email
EMAIL_PASS=your_email_app_password

CLOUDINARY_CLOUD_NAME=your_cloud_name
CLOUDINARY_API_KEY=your_api_key
CLOUDINARY_API_SECRET=your_api_secret
```

Configure the environment variables according to your project configuration.

### 4. Start the Server

```bash
npm start
```

For development:

```bash
npm run dev
```

Open your browser and navigate to:

```text
http://localhost:5000
```

## 📂 Project Highlights

StayEase focuses on implementing practical backend engineering concepts, including:

- Authentication and authorization.
- OAuth 2.0 integration.
- JWT-based authentication.
- Login rate limiting and security.
- MongoDB data modeling and relationships.
- Email verification workflows.
- Cloud-based media management.
- Role-based property and reservation management.
- Booking request lifecycle management.

## 🚀 Future Improvements

- Real-time booking notifications.
- Online payment integration.
- Advanced hotel search and filtering.
- Hotel availability management.
- Redis-based caching and rate limiting.
- Scalable deployment with load balancing.

## 👨‍💻 Author

**Sankalp Nikhare**

- GitHub: [@sankalpnikhare](https://github.com/sankalpnikhare)
- LinkedIn: [Sankalp Nikhare](https://www.linkedin.com/in/sankalp-nikhare-404a08259/)

---

⭐ If you find this project interesting, consider giving it a star!