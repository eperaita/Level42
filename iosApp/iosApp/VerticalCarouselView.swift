import SwiftUI
import shared

struct VerticalCarouselView: View {
    let projects: [Project]
    @Binding var navigationPath: NavigationPath
    @State private var selectedIndex: Int = 0
    @State private var dragOffset: CGFloat = 0
    @State private var animating: Bool = false

    // DISEÑO DE ELEMENTOS
    private let itemHeight: CGFloat = 80
    private let circleHeight: CGFloat = 60
    private let itemSpacing: CGFloat = 20  // espaciado entre elementos
    private let dragSensitivity: CGFloat = 1.0

    // COLOR
    private let customYellow = Color(red: 1.0, green: 0.988, blue: 0.0)
    private let customBlack = Color.black

    var body: some View {
        GeometryReader { geometry in
            let midY = geometry.size.height / 2

            ZStack {
                // Fondo transparente
                Color.clear

                // Elemento central fijo (seleccionado)
                selectedProjectView(projects[selectedIndex])
                    .position(x: geometry.size.width / 2, y: midY)
                    .zIndex(100)

                // Círculos arriba y abajo
                ForEach(0..<projects.count, id: \.self) { index in
                    if index != selectedIndex {
                        let offset = calculateOffset(for: index)
                        let position = midY + offset + dragOffset

                        // Solo mostrar si está en pantalla
                        if position > -circleHeight && position < geometry.size.height + circleHeight {
                            circleProjectView(projects[index], index: index)
                                .position(x: geometry.size.width / 2, y: position)
                        }
                    }
                }
            }
            .contentShape(Rectangle())
            .gesture(
                DragGesture()
                    .onChanged { value in
                        if !animating {
                            dragOffset = value.translation.height * dragSensitivity
                        }
                    }
                    .onEnded { value in
                        // Determinar dirección y velocidad
                        let velocity = value.predictedEndLocation.y - value.location.y
                        let direction = velocity > 0 ? -1 : 1

                        // Decidir cuántos items saltar
                        let change = calculatePositionChange(velocity: velocity, translation: value.translation.height)
                        let targetIndex = max(0, min(selectedIndex + (direction * change), projects.count - 1))

                        if targetIndex != selectedIndex {
                            withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                                animating = true
                                selectedIndex = targetIndex
                                dragOffset = 0
                            }

                            // Pequeño retardo antes de permitir más gestos
                            //DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                            //    animating = false
                            //}
                        } else {
                            withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                                dragOffset = 0
                            }
                        }
                    }
            )
        }
    }

    // Vista para el elemento seleccionado
    private func selectedProjectView(_ project: Project) -> some View {
        Button(action: {
            print("[VerticalCarouselView] Navigating to project: \(project)")
            let projectId = Int(project.project.id) // Convertimos Int32 → Int
            navigationPath.append(ProjectID(value: projectId))
        }) {
            VStack(spacing: 4) {
                Text(project.project.name)
                    .font(.system(size: 24, weight: .bold))
                    .foregroundColor(customYellow)
                    .multilineTextAlignment(.center)
                    .lineLimit(2)

            }
            .frame(maxWidth: .infinity)
            .frame(height: itemHeight)
            .background(customBlack)
            .cornerRadius(8)
            .padding(.horizontal, 20)
        }
    }

    // Vista para los elementos en círculo
    private func circleProjectView(_ project: Project, index: Int) -> some View {
        Button(action: {
            if !animating {
                withAnimation(.spring(response: 0.3, dampingFraction: 0.7)) {
                    animating = true
                    selectedIndex = index
                    dragOffset = 0
                }

                DispatchQueue.main.asyncAfter(deadline: .now() + 0.3) {
                    animating = false
                }
            }
        }) {
            ZStack {
                Circle()
                    .fill(customBlack)
                    .frame(width: circleHeight, height: circleHeight)

                VStack(spacing: 2) {
                    Text(project.project.name)
                        .font(.system(size: 10, weight: .bold))
                        .foregroundColor(.white)
                        .multilineTextAlignment(.center)
                        .lineLimit(1)
                        .padding(.horizontal, 4)

                }
                .frame(width: circleHeight - 10)
            }
        }
    }

    // Calcular el offset para cada elemento con espaciado fijo constante
    private func calculateOffset(for index: Int) -> CGFloat {
        let distance = CGFloat(index - selectedIndex)
        // Offset con espaciado fijo y constante
        return distance * (circleHeight + itemSpacing)
    }

    // Calcular la cantidad de índices a cambiar basado en la velocidad y distancia
    private func calculatePositionChange(velocity: CGFloat, translation: CGFloat) -> Int {
        // Para movimientos lentos, usamos la distancia de arrastre
        if abs(velocity) < 300 {
            let minDragForChange = (circleHeight + itemSpacing) * 0.4
            return abs(translation) > minDragForChange ? 1 : 0
        }

        // Para movimientos rápidos, usamos la velocidad
        let absVelocity = abs(velocity)
        if absVelocity > 1500 {
            return 3
        } else if absVelocity > 800 {
            return 2
        } else {
            return 1
        }
    }
}
