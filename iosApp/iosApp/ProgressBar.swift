import SwiftUI

struct ProgressBar: View {
    var value: Double
    var maxValue: Int

    private var progress: CGFloat {
        CGFloat(value) / CGFloat(maxValue)
    }

    var body: some View {
        ZStack(alignment: .leading) {
            RoundedRectangle(cornerRadius: 10)
                .fill(Color.gray.opacity(0.3))
                .frame(height: 20)

            GeometryReader { geometry in
                RoundedRectangle(cornerRadius: 10)
                    .fill(Color.yellow)
                    .frame(width: geometry.size.width * progress, height: 20)
            }

            Text("\(value)")
                .font(.system(size: 12, weight: .bold))
                .foregroundColor(.black)
                .frame(maxWidth: .infinity, alignment: .center)
        }
        .frame(height: 20)
    }
}
