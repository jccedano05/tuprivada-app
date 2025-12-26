# 📱 Guía de Integración Frontend - Módulo de Surveys

## 🎯 Prompt para el Desarrollador Frontend

Hola! Aquí está la documentación completa para integrar el módulo de Surveys/Encuestas en React Native.

---

## 📋 Endpoints Disponibles

### Base URL
```
/api/surveys
```

---

## 1️⃣ CREAR ENCUESTA

### **Endpoint**
```
POST /api/surveys
```

### **Headers Requeridos**
```typescript
{
  'Content-Type': 'application/json',
  'Authorization': 'Bearer YOUR_JWT_TOKEN'
}
```

### **Request Body** ⚠️ IMPORTANTE
```typescript
interface CreateSurveyRequest {
  title: string;                    // REQUERIDO - Título de la encuesta
  description?: string;              // OPCIONAL - Descripción
  type: 'POLL' | 'SURVEY' | 'VOTING'; // REQUERIDO
  status: 'DRAFT' | 'ACTIVE' | 'SCHEDULED'; // ⚠️ REQUERIDO - Este campo NO puede omitirse
  startDate: string;                 // REQUERIDO - ISO 8601: "2025-01-01T00:00:00"
  endDate: string;                   // REQUERIDO - ISO 8601: "2025-01-31T23:59:59"
  isAnonymous: boolean;              // REQUERIDO - true/false
  allowMultipleVotes: boolean;       // REQUERIDO - true/false
  condominiumId: number;             // REQUERIDO - ID del condominio
  questions: CreateSurveyQuestionRequest[]; // REQUERIDO - Array de preguntas
}

interface CreateSurveyQuestionRequest {
  question: string;                  // REQUERIDO - Texto de la pregunta
  type: 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'TEXT' | 'RATING' | 'YES_NO'; // REQUERIDO
  isRequired: boolean;               // REQUERIDO
  order: number;                     // REQUERIDO - Orden de visualización (1, 2, 3...)
  options: CreateSurveyOptionRequest[]; // Para SINGLE_CHOICE, MULTIPLE_CHOICE, YES_NO
}

interface CreateSurveyOptionRequest {
  text: string;                      // REQUERIDO - Texto de la opción
  order: number;                     // REQUERIDO - Orden (1, 2, 3...)
}
```

### **Ejemplo de Request Completo**

#### Ejemplo 1: Encuesta Sí/No
```typescript
const surveyData = {
  title: "¿Estás satisfecho con la seguridad?",
  description: "Queremos conocer tu opinión sobre el servicio de seguridad",
  type: "POLL",
  status: "ACTIVE",  // ⚠️ NO OLVIDAR
  startDate: "2025-01-01T00:00:00",
  endDate: "2025-01-31T23:59:59",
  isAnonymous: false,
  allowMultipleVotes: false,
  condominiumId: 1,
  questions: [
    {
      question: "¿Estás satisfecho con el servicio de seguridad?",
      type: "YES_NO",
      isRequired: true,
      order: 1,
      options: [
        { text: "Sí", order: 1 },
        { text: "No", order: 2 }
      ]
    }
  ]
};
```

#### Ejemplo 2: Encuesta de Opción Única
```typescript
const surveyData = {
  title: "Calidad del servicio de limpieza",
  description: "Ayúdanos a mejorar nuestros servicios",
  type: "SURVEY",
  status: "DRAFT",  // ⚠️ SIEMPRE incluir status
  startDate: "2025-01-15T00:00:00",
  endDate: "2025-02-15T23:59:59",
  isAnonymous: true,
  allowMultipleVotes: false,
  condominiumId: 1,
  questions: [
    {
      question: "¿Cómo calificarías el servicio de limpieza?",
      type: "SINGLE_CHOICE",
      isRequired: true,
      order: 1,
      options: [
        { text: "Excelente", order: 1 },
        { text: "Bueno", order: 2 },
        { text: "Regular", order: 3 },
        { text: "Malo", order: 4 }
      ]
    }
  ]
};
```

#### Ejemplo 3: Encuesta con Rating
```typescript
const surveyData = {
  title: "Satisfacción general del condominio",
  type: "SURVEY",
  status: "ACTIVE",
  startDate: new Date().toISOString(),
  endDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(),
  isAnonymous: false,
  allowMultipleVotes: false,
  condominiumId: 1,
  questions: [
    {
      question: "Del 1 al 5, ¿qué tan satisfecho estás?",
      type: "RATING",
      isRequired: true,
      order: 1,
      options: [] // Rating no necesita options
    }
  ]
};
```

### **Response Exitosa (201 Created)**
```typescript
{
  code: 201,
  status: "Created",
  message: "Encuesta creada exitosamente",
  data: {
    id: 1,
    title: "¿Estás satisfecho con la seguridad?",
    description: "Queremos conocer tu opinión...",
    type: "POLL",
    status: "ACTIVE",
    startDate: "2025-01-01T00:00:00",
    endDate: "2025-01-31T23:59:59",
    isAnonymous: false,
    allowMultipleVotes: false,
    condominiumId: 1,
    createdById: 123,
    totalVotes: 0,
    questions: [
      {
        id: 1,
        question: "¿Estás satisfecho con el servicio de seguridad?",
        type: "YES_NO",
        isRequired: true,
        order: 1,
        options: [
          { id: 1, text: "Sí", order: 1, votes: 0, percentage: 0 },
          { id: 2, text: "No", order: 2, votes: 0, percentage: 0 }
        ]
      }
    ],
    createdAt: "2025-01-01T10:00:00",
    updatedAt: "2025-01-01T10:00:00"
  }
}
```

### **Posibles Errores**
```typescript
// 400 - Validación fallida
{
  code: 400,
  message: "La petición contiene datos inválidos",
  data: {
    errors: {
      status: "El estado es requerido",
      title: "El título es requerido",
      questions: "Debe haber al menos una pregunta"
    }
  }
}

// 403 - Sin permisos (no es admin)
{
  code: 403,
  message: "Forbidden"
}

// 404 - Condominio no encontrado
{
  code: 404,
  message: "Condominio no encontrado"
}
```

---

## 2️⃣ LISTAR ENCUESTAS

### **Endpoint**
```
GET /api/surveys?condominiumId={id}&status={status}
```

### **Query Parameters**
```typescript
{
  condominiumId: number;  // REQUERIDO
  status?: 'DRAFT' | 'ACTIVE' | 'CLOSED' | 'SCHEDULED'; // OPCIONAL - filtro
}
```

### **Ejemplo de Request**
```typescript
// Todas las encuestas del condominio
GET /api/surveys?condominiumId=1

// Solo encuestas activas
GET /api/surveys?condominiumId=1&status=ACTIVE
```

### **Response (200 OK)**
```typescript
{
  code: 200,
  status: "OK",
  data: [
    {
      id: 1,
      title: "Encuesta 1",
      type: "POLL",
      status: "ACTIVE",
      // ... resto de campos
    },
    // ... más encuestas
  ]
}
```

---

## 3️⃣ OBTENER ENCUESTA POR ID

### **Endpoint**
```
GET /api/surveys/{surveyId}
```

### **Response (200 OK)**
```typescript
{
  code: 200,
  data: {
    id: 1,
    title: "Mi encuesta",
    questions: [...],
    // ... datos completos
  }
}
```

---

## 4️⃣ ACTUALIZAR ENCUESTA

### **Endpoint**
```
PUT /api/surveys/{surveyId}
```

### **Request Body**
```typescript
interface UpdateSurveyRequest {
  title?: string;
  description?: string;
  status?: 'DRAFT' | 'ACTIVE' | 'CLOSED' | 'SCHEDULED';
  startDate?: string;
  endDate?: string;
  // Otros campos opcionales
}
```

### **Ejemplo**
```typescript
const updateData = {
  title: "Título actualizado",
  status: "ACTIVE"
};

// PUT /api/surveys/1
```

### **Response (200 OK)**
```typescript
{
  code: 200,
  data: {
    // Encuesta actualizada completa
  }
}
```

---

## 5️⃣ ELIMINAR ENCUESTA (Soft Delete)

### **Endpoint**
```
DELETE /api/surveys/{surveyId}
```

### **Ejemplo**
```typescript
DELETE /api/surveys/1
```

### **Response (204 No Content)**
```
(Sin body - solo status code 204)
```

---

## 6️⃣ ENVIAR RESPUESTA A ENCUESTA

### **Endpoint**
```
POST /api/surveys/{surveyId}/responses
```

### **Request Body**
```typescript
interface SubmitSurveyResponseRequest {
  userId: number;
  residentId?: number;  // OPCIONAL
  answers: SurveyAnswerRequest[];
}

interface SurveyAnswerRequest {
  questionId: number;
  selectedOptionIds?: number[];  // Para SINGLE_CHOICE, MULTIPLE_CHOICE, YES_NO
  textAnswer?: string;           // Para TEXT
  ratingValue?: number;          // Para RATING (1-5)
}
```

### **Ejemplo: Responder Sí/No**
```typescript
const responseData = {
  userId: 123,
  answers: [
    {
      questionId: 1,
      selectedOptionIds: [1]  // ID de la opción "Sí"
    }
  ]
};

// POST /api/surveys/1/responses
```

### **Ejemplo: Responder con Rating**
```typescript
const responseData = {
  userId: 123,
  answers: [
    {
      questionId: 2,
      ratingValue: 5
    }
  ]
};
```

### **Response (201 Created)**
```typescript
{
  code: 201,
  data: {
    id: 1,
    surveyId: 1,
    userId: 123,
    answers: [...],
    submittedAt: "2025-01-15T14:30:00"
  }
}
```

---

## 7️⃣ VER RESULTADOS DE ENCUESTA

### **Endpoint**
```
GET /api/surveys/{surveyId}/results
```

### **Response (200 OK)**
```typescript
{
  code: 200,
  data: {
    surveyId: 1,
    totalResponses: 45,
    responseRate: 75.5,  // Porcentaje
    questions: [
      {
        questionId: 1,
        question: "¿Estás satisfecho?",
        type: "YES_NO",
        totalAnswers: 45,
        options: [
          {
            optionId: 1,
            text: "Sí",
            votes: 30,
            percentage: 66.67
          },
          {
            optionId: 2,
            text: "No",
            votes: 15,
            percentage: 33.33
          }
        ]
      }
    ]
  }
}
```

---

## 8️⃣ VERIFICAR SI USUARIO VOTÓ

### **Endpoint**
```
GET /api/surveys/{surveyId}/check-voted?userId={userId}
```

### **Response (200 OK)**
```typescript
{
  code: 200,
  data: {
    hasVoted: true
  }
}
```

---

## 9️⃣ CAMBIAR ESTADO DE ENCUESTA (ADMIN)

### **Endpoint**
```
PATCH /api/surveys/{surveyId}/status
```

### **Request Body**
```typescript
{
  status: 'DRAFT' | 'ACTIVE' | 'CLOSED' | 'SCHEDULED'
}
```

### **Ejemplo**
```typescript
const statusUpdate = {
  status: "CLOSED"
};

// PATCH /api/surveys/1/status
```

---

## 🗂️ INTEGRACIÓN CON ZUSTAND

### **1. Crear el Store de Surveys**

```typescript
// stores/surveyStore.ts
import { create } from 'zustand';
import axios from 'axios';

interface Survey {
  id: number;
  title: string;
  description?: string;
  type: 'POLL' | 'SURVEY' | 'VOTING';
  status: 'DRAFT' | 'ACTIVE' | 'CLOSED' | 'SCHEDULED';
  startDate: string;
  endDate: string;
  isAnonymous: boolean;
  allowMultipleVotes: boolean;
  condominiumId: number;
  totalVotes: number;
  questions: SurveyQuestion[];
  createdAt: string;
  updatedAt: string;
}

interface SurveyQuestion {
  id: number;
  question: string;
  type: 'SINGLE_CHOICE' | 'MULTIPLE_CHOICE' | 'TEXT' | 'RATING' | 'YES_NO';
  isRequired: boolean;
  order: number;
  options: SurveyOption[];
}

interface SurveyOption {
  id: number;
  text: string;
  order: number;
  votes: number;
  percentage: number;
}

interface SurveyStore {
  surveys: Survey[];
  activeSurveys: Survey[];
  currentSurvey: Survey | null;
  loading: boolean;
  error: string | null;

  // Actions
  fetchSurveys: (condominiumId: number, status?: string) => Promise<void>;
  fetchActiveSurveys: (condominiumId: number) => Promise<void>;
  fetchSurveyById: (surveyId: number) => Promise<void>;
  createSurvey: (data: CreateSurveyRequest) => Promise<Survey>;
  updateSurvey: (surveyId: number, data: any) => Promise<Survey>;
  deleteSurvey: (surveyId: number) => Promise<void>;
  submitResponse: (surveyId: number, data: any) => Promise<void>;
  checkIfVoted: (surveyId: number, userId: number) => Promise<boolean>;
  clearError: () => void;
}

export const useSurveyStore = create<SurveyStore>((set, get) => ({
  surveys: [],
  activeSurveys: [],
  currentSurvey: null,
  loading: false,
  error: null,

  fetchSurveys: async (condominiumId: number, status?: string) => {
    set({ loading: true, error: null });
    try {
      const params = status 
        ? `?condominiumId=${condominiumId}&status=${status}`
        : `?condominiumId=${condominiumId}`;
      
      const response = await axios.get(`/api/surveys${params}`);
      set({ surveys: response.data.data || response.data, loading: false });
    } catch (error: any) {
      set({ 
        error: error.response?.data?.message || 'Error al cargar encuestas',
        loading: false 
      });
    }
  },

  fetchActiveSurveys: async (condominiumId: number) => {
    set({ loading: true, error: null });
    try {
      const response = await axios.get(
        `/api/surveys/active?condominiumId=${condominiumId}`
      );
      set({ activeSurveys: response.data.data || response.data, loading: false });
    } catch (error: any) {
      set({ 
        error: error.response?.data?.message || 'Error al cargar encuestas activas',
        loading: false 
      });
    }
  },

  fetchSurveyById: async (surveyId: number) => {
    set({ loading: true, error: null });
    try {
      const response = await axios.get(`/api/surveys/${surveyId}`);
      set({ currentSurvey: response.data.data || response.data, loading: false });
    } catch (error: any) {
      set({ 
        error: error.response?.data?.message || 'Error al cargar encuesta',
        loading: false 
      });
    }
  },

  createSurvey: async (data: CreateSurveyRequest) => {
    set({ loading: true, error: null });
    try {
      // ⚠️ VALIDAR que status esté presente
      if (!data.status) {
        throw new Error('El campo status es requerido');
      }

      const response = await axios.post('/api/surveys', data);
      const newSurvey = response.data.data || response.data;
      
      set(state => ({ 
        surveys: [...state.surveys, newSurvey],
        loading: false 
      }));
      
      return newSurvey;
    } catch (error: any) {
      const errorMessage = error.response?.data?.message || error.message || 'Error al crear encuesta';
      set({ error: errorMessage, loading: false });
      throw error;
    }
  },

  updateSurvey: async (surveyId: number, data: any) => {
    set({ loading: true, error: null });
    try {
      const response = await axios.put(`/api/surveys/${surveyId}`, data);
      const updatedSurvey = response.data.data || response.data;
      
      set(state => ({
        surveys: state.surveys.map(s => 
          s.id === surveyId ? updatedSurvey : s
        ),
        currentSurvey: state.currentSurvey?.id === surveyId 
          ? updatedSurvey 
          : state.currentSurvey,
        loading: false
      }));
      
      return updatedSurvey;
    } catch (error: any) {
      set({ 
        error: error.response?.data?.message || 'Error al actualizar encuesta',
        loading: false 
      });
      throw error;
    }
  },

  deleteSurvey: async (surveyId: number) => {
    set({ loading: true, error: null });
    try {
      await axios.delete(`/api/surveys/${surveyId}`);
      
      set(state => ({
        surveys: state.surveys.filter(s => s.id !== surveyId),
        activeSurveys: state.activeSurveys.filter(s => s.id !== surveyId),
        currentSurvey: state.currentSurvey?.id === surveyId 
          ? null 
          : state.currentSurvey,
        loading: false
      }));
    } catch (error: any) {
      set({ 
        error: error.response?.data?.message || 'Error al eliminar encuesta',
        loading: false 
      });
      throw error;
    }
  },

  submitResponse: async (surveyId: number, data: any) => {
    set({ loading: true, error: null });
    try {
      await axios.post(`/api/surveys/${surveyId}/responses`, data);
      set({ loading: false });
      
      // Refrescar la encuesta para actualizar contadores
      await get().fetchSurveyById(surveyId);
    } catch (error: any) {
      set({ 
        error: error.response?.data?.message || 'Error al enviar respuesta',
        loading: false 
      });
      throw error;
    }
  },

  checkIfVoted: async (surveyId: number, userId: number) => {
    try {
      const response = await axios.get(
        `/api/surveys/${surveyId}/check-voted?userId=${userId}`
      );
      return response.data.data?.hasVoted || response.data.hasVoted || false;
    } catch (error) {
      return false;
    }
  },

  clearError: () => set({ error: null })
}));
```

---

## 📱 COMPONENTES REACT NATIVE

### **2. Hook para Crear Encuesta**

```typescript
// hooks/useCreateSurvey.ts
import { useState } from 'react';
import { useSurveyStore } from '../stores/surveyStore';
import { Alert } from 'react-native';

export const useCreateSurvey = () => {
  const createSurvey = useSurveyStore(state => state.createSurvey);
  const [loading, setLoading] = useState(false);

  const handleCreateSurvey = async (formData: any) => {
    setLoading(true);
    try {
      // ⚠️ IMPORTANTE: Asegurar que status está presente
      const surveyData = {
        ...formData,
        status: formData.status || 'DRAFT', // Default si no se especifica
      };

      const newSurvey = await createSurvey(surveyData);
      Alert.alert('Éxito', 'Encuesta creada correctamente');
      return newSurvey;
    } catch (error: any) {
      const errorMsg = error.response?.data?.message || 
                      error.message || 
                      'Error al crear encuesta';
      Alert.alert('Error', errorMsg);
      console.error('❌ Error en createSurvey:', error);
    } finally {
      setLoading(false);
    }
  };

  return { handleCreateSurvey, loading };
};
```

### **3. Componente de Formulario de Encuesta**

```typescript
// screens/CreateSurveyScreen.tsx
import React, { useState } from 'react';
import { View, TextInput, Button, ScrollView } from 'react-native';
import { useCreateSurvey } from '../hooks/useCreateSurvey';

export const CreateSurveyScreen = ({ condominiumId, navigation }) => {
  const { handleCreateSurvey, loading } = useCreateSurvey();
  
  const [title, setTitle] = useState('');
  const [description, setDescription] = useState('');
  const [type, setType] = useState<'POLL' | 'SURVEY' | 'VOTING'>('POLL');
  const [status, setStatus] = useState<'DRAFT' | 'ACTIVE'>('DRAFT'); // ⚠️ IMPORTANTE
  const [isAnonymous, setIsAnonymous] = useState(false);
  const [questionText, setQuestionText] = useState('');

  const onSubmit = async () => {
    const surveyData = {
      title,
      description,
      type,
      status,  // ⚠️ NO OLVIDAR
      startDate: new Date().toISOString(),
      endDate: new Date(Date.now() + 30 * 24 * 60 * 60 * 1000).toISOString(),
      isAnonymous,
      allowMultipleVotes: false,
      condominiumId,
      questions: [
        {
          question: questionText,
          type: 'YES_NO',
          isRequired: true,
          order: 1,
          options: [
            { text: 'Sí', order: 1 },
            { text: 'No', order: 2 }
          ]
        }
      ]
    };

    const result = await handleCreateSurvey(surveyData);
    if (result) {
      navigation.goBack();
    }
  };

  return (
    <ScrollView>
      <TextInput
        placeholder="Título de la encuesta"
        value={title}
        onChangeText={setTitle}
      />
      <TextInput
        placeholder="Descripción (opcional)"
        value={description}
        onChangeText={setDescription}
      />
      <TextInput
        placeholder="Pregunta"
        value={questionText}
        onChangeText={setQuestionText}
      />
      {/* Agregar pickers para type, status, etc */}
      <Button 
        title={loading ? "Creando..." : "Crear Encuesta"}
        onPress={onSubmit}
        disabled={loading || !title || !questionText}
      />
    </ScrollView>
  );
};
```

---

## ⚠️ CHECKLIST DE VALIDACIONES

Antes de enviar el request de creación, verifica:

- [ ] `title` está presente y no vacío
- [ ] `status` está presente (**CRÍTICO** - este era tu error)
- [ ] `type` es uno de: 'POLL', 'SURVEY', 'VOTING'
- [ ] `startDate` y `endDate` están en formato ISO 8601
- [ ] `endDate` es posterior a `startDate`
- [ ] `isAnonymous` es boolean
- [ ] `allowMultipleVotes` es boolean
- [ ] `condominiumId` es un número válido
- [ ] `questions` es un array con al menos 1 pregunta
- [ ] Cada pregunta tiene `type` válido
- [ ] Si es SINGLE_CHOICE/MULTIPLE_CHOICE/YES_NO, tiene `options`
- [ ] Cada opción tiene `text` y `order`

---

## 🐛 DEBUGGING

Si obtienes error 400, revisa en `error.response.data.data.errors` qué campos faltan:

```typescript
try {
  await createSurvey(data);
} catch (error: any) {
  console.log('❌ Errores de validación:', error.response?.data?.data?.errors);
  // Output ejemplo:
  // { status: "El estado es requerido", title: "El título es requerido" }
}
```

---

## 📌 NOTAS FINALES

1. **SIEMPRE incluir `status`** en CreateSurveyRequest
2. Usar tokens JWT en headers para autenticación
3. Fechas siempre en formato ISO 8601
4. El backend valida permisos (solo ADMIN puede crear/editar/eliminar)
5. Las respuestas incrementan automáticamente los contadores de votos
6. `isAnonymous=true` oculta datos de usuario en los resultados

---

¿Necesitas ejemplos adicionales para algún endpoint específico? 🚀
