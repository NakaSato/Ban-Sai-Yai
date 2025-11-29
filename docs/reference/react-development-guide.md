# React Development Guide

## Overview

This guide provides comprehensive documentation for React development, covering modern patterns, best practices, and advanced features. It serves as a reference for building scalable, maintainable React applications in the Ban Sai Yai Savings Group System.

## Table of Contents

- [Component Creation](#component-creation)
- [State Management with useState](#state-management-with-usestate)
- [Side Effects with useEffect](#side-effects-with-useeffect)
- [Performance Optimization with useCallback](#performance-optimization-with-usecallback)
- [Form Handling with useActionState](#form-handling-with-useactionstate)
- [Context API](#context-api)
- [React DOM Rendering](#react-dom-rendering)
- [Resource Preloading](#resource-preloading)
- [Server Components](#server-components)
- [Client-Server Component Composition](#client-server-component-composition)
- [Server Actions](#server-actions)
- [Server Actions Outside Forms](#server-actions-outside-forms)
- [Async Server Components with Suspense](#async-server-components-with-suspense)
- [Full-Stack Application Pattern](#full-stack-application-pattern)

---

## Component Creation

Creating functional components with JSX

```jsx
// Basic component definition
function Profile() {
  return (
    <img
      src="https://i.imgur.com/MK3eW3Am.jpg"
      alt="Katherine Johnson"
    />
  );
}

// Component with props
function Video({ video }) {
  return (
    <div>
      <Thumbnail video={video} />
      <a href={video.url}>
        <h3>{video.title}</h3>
        <p>{video.description}</p>
      </a>
      <LikeButton video={video} />
    </div>
  );
}

// Composing components
export default function Gallery() {
  return (
    <section>
      <h1>Amazing scientists</h1>
      <Profile />
      <Profile />
      <Profile />
    </section>
  );
}
```

### Best Practices for Components

- **Single Responsibility**: Each component should have one clear purpose
- **Composition over Inheritance**: Build complex UIs by combining simple components
- **Props Interface**: Define clear prop interfaces using TypeScript
- **Default Props**: Provide sensible defaults for optional props

---

## State Management with useState

Managing component state

```jsx
import { useState } from 'react';

function SearchableVideoList({ videos }) {
  const [searchText, setSearchText] = useState('');
  const foundVideos = filterVideos(videos, searchText);

  return (
    <>
      <SearchInput
        value={searchText}
        onChange={newText => setSearchText(newText)} />
      <VideoList
        videos={foundVideos}
        emptyHeading={`No matches for "${searchText}"`} />
    </>
  );
}

function ImageGallery() {
  const [index, setIndex] = useState(0);

  return (
    <div>
      <button onClick={() => setIndex(index + 1)}>Next</button>
      <Image index={index} />
    </div>
  );
}
```

### State Management Patterns

- **Lift State Up**: Share state between components by moving it to their nearest common ancestor
- **State Colocation**: Keep state close to where it's used
- **Derived State**: Calculate values from existing state instead of duplicating

---

## Side Effects with useEffect

Connecting components to external systems

```jsx
import { useEffect } from 'react';

function ChatRoom({ roomId }) {
  useEffect(() => {
    const connection = createConnection(roomId);
    connection.connect();
    return () => connection.disconnect();
  }, [roomId]);

  return <div>Welcome to room {roomId}</div>;
}
```

### useEffect Best Practices

- **Dependency Array**: Include all dependencies to avoid stale closures
- **Cleanup Functions**: Return cleanup functions to prevent memory leaks
- **Multiple Effects**: Separate concerns with multiple useEffect hooks

---

## Performance Optimization with useCallback

Memoizing functions between renders

```jsx
import { useCallback, memo } from 'react';

function ProductPage({ productId, referrer, theme }) {
  const handleSubmit = useCallback((orderDetails) => {
    post('/product/' + productId + '/buy', {
      referrer,
      orderDetails,
    });
  }, [productId, referrer]);

  return (
    <div className={theme}>
      <ShippingForm onSubmit={handleSubmit} />
    </div>
  );
}

const ShippingForm = memo(function ShippingForm({ onSubmit }) {
  return (
    <form onSubmit={onSubmit}>
      {/* form fields */}
    </form>
  );
});

function post(url, data) {
  console.log('POST /' + url);
  console.log(data);
}
```

### Performance Optimization Strategies

- **useCallback**: Memoize functions to prevent unnecessary re-renders
- **useMemo**: Memoize expensive calculations
- **React.memo**: Prevent component re-renders when props haven't changed
- **Code Splitting**: Use React.lazy and Suspense for component-level code splitting

---

## Form Handling with useActionState

Managing form state and server actions

```jsx
import { useActionState } from "react";

async function increment(previousState, formData) {
  return previousState + 1;
}

function StatefulForm() {
  const [state, formAction, isPending] = useActionState(increment, 0);

  return (
    <form>
      {state}
      <button formAction={formAction}>Increment</button>
      {isPending && "Loading..."}
    </form>
  );
}

// With validation and error handling
async function addToCart(previousState, formData) {
  const itemID = formData.get('itemID');

  try {
    await saveToCart(itemID);
    return { success: true, message: 'Added to cart!' };
  } catch (error) {
    return { success: false, message: 'Failed to add to cart' };
  }
}

function AddToCartForm({ itemID, itemTitle }) {
  const [state, formAction, isPending] = useActionState(addToCart, null);

  return (
    <form action={formAction}>
      <h2>{itemTitle}</h2>
      <input type="hidden" name="itemID" value={itemID} />
      <button type="submit" disabled={isPending}>Add to Cart</button>
      {isPending ? "Loading..." : state?.message}
    </form>
  );
}
```

---

## Context API

Sharing data across component tree

```jsx
import { useContext, createContext } from 'react';

const ThemeContext = createContext('light');

function Button() {
  const theme = useContext(ThemeContext);
  return <button className={theme}>Click me</button>;
}

function App() {
  return (
    <ThemeContext.Provider value="dark">
      <Button />
    </ThemeContext.Provider>
  );
}
```

### Context Usage Guidelines

- **Use for Global State**: Theme, user authentication, language preferences
- **Avoid Overuse**: Don't use context for prop drilling that can be solved with composition
- **Split Contexts**: Create separate contexts for different concerns
- **Memoize Context Values**: Prevent unnecessary re-renders

---

## React DOM Rendering

Client-side rendering APIs

```jsx
import { createRoot } from 'react-dom/client';
import { createPortal } from 'react-dom';
import App from './App';

// Mount application
const root = createRoot(document.getElementById('root'));
root.render(<App />);

// Create portal for modals
function Modal({ children }) {
  return createPortal(
    <div className="modal">{children}</div>,
    document.body
  );
}

// Synchronous updates
import { flushSync } from 'react-dom';

function handleClick() {
  flushSync(() => {
    setCount(count + 1);
  });
  // DOM is updated synchronously here
}
```

---

## Resource Preloading

Optimizing resource loading

```jsx
import { preload, prefetchDNS, preconnect } from 'react-dom';

function MyComponent() {
  // Preload critical resources
  preload('/fonts/main.woff2', { as: 'font', type: 'font/woff2' });
  preload('/styles/main.css', { as: 'style' });

  // Prefetch DNS for external domains
  prefetchDNS('https://api.example.com');

  // Preconnect to domains
  preconnect('https://cdn.example.com');

  return <div>Content</div>;
}
```

---

## Server Components

Server-side component rendering

```jsx
// Server Component - runs on server only
import db from './database';

async function Note({ id }) {
  // Loads during server render
  const note = await db.notes.get(id);

  return (
    <div>
      <Author id={note.authorId} />
      <p>{note}</p>
    </div>
  );
}

async function Author({ id }) {
  const author = await db.authors.get(id);
  return <span>By: {author.name}</span>;
}

// With async data fetching
async function Page({ page }) {
  const content = await file.readFile(`${page}.md`);
  return <div>{sanitizeHtml(marked(content))}</div>;
}
```

---

## Client-Server Component Composition

Mixing server and client components

```jsx
// Server Component
import Expandable from './Expandable';

async function Notes() {
  const notes = await db.notes.getAll();

  return (
    <div>
      {notes.map(note => (
        <Expandable key={note.id}>
          <p>{note.text}</p>
        </Expandable>
      ))}
    </div>
  );
}

// Client Component
"use client"

import { useState } from 'react';

export default function Expandable({ children }) {
  const [expanded, setExpanded] = useState(false);

  return (
    <div>
      <button onClick={() => setExpanded(!expanded)}>
        Toggle
      </button>
      {expanded && children}
    </div>
  );
}
```

---

## Server Actions

Server-side form mutations

```jsx
// actions.js
'use server';

export async function requestUsername(formData) {
  const username = formData.get('username');

  if (canRequest(username)) {
    await db.users.create({ username });
    return { success: true, message: 'Username created' };
  }

  return { success: false, message: 'Username unavailable' };
}

export async function incrementLike() {
  const count = await db.likes.increment();
  return count;
}

// App.js
import { requestUsername } from './actions';

export default function App() {
  return (
    <form action={requestUsername}>
      <input type="text" name="username" />
      <button type="submit">Request</button>
    </form>
  );
}
```

---

## Server Actions Outside Forms

Using server actions programmatically

```jsx
'use client';

import { useState, useTransition } from 'react';
import { incrementLike } from './actions';

function LikeButton() {
  const [isPending, startTransition] = useTransition();
  const [likeCount, setLikeCount] = useState(0);

  const onClick = () => {
    startTransition(async () => {
      const currentCount = await incrementLike();
      setLikeCount(currentCount);
    });
  };

  return (
    <>
      <p>Total Likes: {likeCount}</p>
      <button onClick={onClick} disabled={isPending}>
        {isPending ? 'Loading...' : 'Like'}
      </button>
    </>
  );
}
```

---

## Async Server Components with Suspense

Streaming server-rendered content

```jsx
// Server Component
import { Suspense } from 'react';
import db from './database';

async function Page({ id }) {
  // Awaited on server
  const note = await db.notes.get(id);

  // Promise passed to client
  const commentsPromise = db.comments.get(note.id);

  return (
    <div>
      <h1>{note.title}</h1>
      <p>{note.content}</p>
      <Suspense fallback={<p>Loading Comments...</p>}>
        <Comments commentsPromise={commentsPromise} />
      </Suspense>
    </div>
  );
}

// Client Component
"use client";

import { use } from 'react';

function Comments({ commentsPromise }) {
  // Suspends until promise resolves
  const comments = use(commentsPromise);

  return (
    <div>
      {comments.map(comment => (
        <p key={comment.id}>{comment.text}</p>
      ))}
    </div>
  );
}
```

---

## Full-Stack Application Pattern

Complete example with routing and data fetching

```jsx
// confs/[slug].js - Framework route file
import { Suspense } from 'react';
import db from './database';

async function ConferencePage({ slug }) {
  const conf = await db.Confs.find({ slug });

  return (
    <ConferenceLayout conf={conf}>
      <Suspense fallback={<TalksLoading />}>
        <Talks confId={conf.id} />
      </Suspense>
    </ConferenceLayout>
  );
}

async function Talks({ confId }) {
  const talks = await db.Talks.findAll({ confId });
  const videos = talks.map(talk => talk.video);

  return <SearchableVideoList videos={videos} />;
}

function SearchableVideoList({ videos }) {
  const [searchText, setSearchText] = useState('');
  const foundVideos = filterVideos(videos, searchText);

  return (
    <>
      <SearchInput
        value={searchText}
        onChange={setSearchText} />
      <VideoList videos={foundVideos} />
    </>
  );
}
```

---

## Application to Ban Sai Yai System

### Current Implementation

The Ban Sai Yai Savings Group System uses:
- **React 19.2.0** with TypeScript
- **Material-UI** for component library
- **React Router** for navigation
- **Redux Toolkit** for state management
- **React Hook Form** for form handling

### Recommended Improvements

1. **Modern State Management**: Consider replacing Redux with React Context + useReducer for simpler state management
2. **Server Components**: Evaluate migration to Next.js for server-side rendering benefits
3. **Performance Optimization**: Implement React.memo, useCallback, and useMemo where appropriate
4. **Form Handling**: Leverage useActionState for better form state management

### Component Architecture

```typescript
// Example: Enhanced Loan Application Component
import { useState, useCallback } from 'react';
import { useActionState } from 'react';

interface LoanApplication {
  amount: number;
  purpose: string;
  duration: number;
}

const LoanApplicationPage: React.FC = () => {
  const [application, setApplication] = useState<LoanApplication>({
    amount: 0,
    purpose: '',
    duration: 12
  });

  const handleSubmit = useCallback(async (prevState: any, formData: FormData) => {
    // Submit logic here
    return { success: true, message: 'Application submitted' };
  }, []);

  const [state, formAction, isPending] = useActionState(handleSubmit, null);

  return (
    <form action={formAction}>
      {/* Form fields */}
      <button type="submit" disabled={isPending}>
        {isPending ? 'Submitting...' : 'Submit Application'}
      </button>
      {state?.message && <div>{state.message}</div>}
    </form>
  );
};
```

---

## Summary

React provides a comprehensive ecosystem for building modern user interfaces across web and native platforms. The core library focuses on component composition, declarative UI through JSX, and efficient state management through hooks like useState, useEffect, and useCallback. Performance optimization is built-in with memoization hooks and the ability to skip unnecessary re-renders through React.memo and proper dependency management.

Modern React development increasingly leverages Server Components and Server Actions for building full-stack applications that combine the simplicity of server-side rendering with the interactivity of single-page applications. This architecture allows developers to fetch data directly in components on the server, reducing client bundle sizes and improving initial page load performance.

For the Ban Sai Yai Savings Group System, applying these modern React patterns can improve performance, maintainability, and user experience while maintaining the robust functionality required for a financial management system.
