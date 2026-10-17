import Comments from '../components/Comments.jsx'

export default function TestPage() {
  return (
    <div style={{ padding: '20px' }}>
      <h2>Tarea de ejemplo: Diseñar el login</h2>
      <Comments taskId="tarea-demo" author="Luis" />
    </div>
  )
}